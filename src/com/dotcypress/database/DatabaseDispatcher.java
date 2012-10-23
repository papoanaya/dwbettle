package com.dotcypress.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

public final class DatabaseDispatcher {

	private static final String LOG_TAG = "dotCypress ORM";

	public static final String REAL = "REAL";
	public static final String TEXT = "TEXT";
	public static final String INTEGER = "INTEGER";
	public static final String NUMERIC = "NUMERIC";
	public static final String BOOLEAN = "BOOLEAN";

	private static final String DATABASE_TAG = "database";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String VERSION_ATTRIBUTE = "version";

	private static final String ENTITY_TAG = "entity";
	private static final String CLASS_NAME_ATTRIBUTE = "className";
	private static final String TABLE_NAME_ATTRIBUTE = "tableName";

	private static final String MAPPING_TAG = "mapping";
	private static final String DATA_TYPE_ATTRIBUTE = "dataType";
	private static final String FIELD_NAME_ATTRIBUTE = "fieldName";
	private static final String COLUMN_NAME_ATTRIBUTE = "columnName";
	private static final Object UNIQUE_ATTRIBUTE = "unique";

	private SQLiteDatabase _database;
	private ArrayList<EntityMapping> _entityMappings = new ArrayList<EntityMapping>();
	private String _databaseName;
	private int _databaseVersion = 0;
	private Context _context;

	public DatabaseDispatcher(Context context,
							  XmlResourceParser mappingDefinition) {
		_context = context;
		loadMapping(mappingDefinition);
		createDatabase();
	}

	public long getCount(Class<? extends EntityBase> entityClass) {
		return getCount(entityClass, null);
	}

	public long getCount(Class<? extends EntityBase> entityClass,
						 String whereClause) {
		EntityMapping metadata = getMetadata(entityClass.getName());
		String query = whereClause == null ? metadata.getCountQuery()
				: metadata.getCountQuery(whereClause);
		Cursor cursor = _database.rawQuery(query, null);
		cursor.moveToFirst();
		long count = cursor.getLong(0);
		cursor.close();
		return count;
	}

	public void save(EntityBase entity) {
		EntityMapping metadata = getMetadata(entity.getClass().getName());
		ContentValues values = fillValues(entity, metadata);
		if (entity.id == -1) {
			entity.id = (int) _database.insert(metadata.getTableName(), null,
					values);
		} else {
			_database.update(metadata.getTableName(), values,
					EntityBase.ID_COLUMN_NAME + "=" + entity.id, null);
		}
	}

	public void saveByUniqueId(EntityBase entity) {
		final EntityMapping metadata = getMetadata(entity.getClass().getName());
		final ContentValues values = fillValues(entity, metadata);
		final ColumnMapping unique = metadata.getUniqueColumnMapping();
		final String idValue = values.getAsString(unique.fieldName);
		if (getCount(entity.getClass(), unique.columnName + "=" + idValue) == 0) {
			entity.id = (int) _database.insert(metadata.getTableName(), null,
					values);
		} else {
			_database.update(metadata.getTableName(), values, unique.columnName
					+ "=" + idValue, null);
		}
	}

	public void delete(EntityBase entity) {
		EntityMapping metadata = getMetadata(entity.getClass().getName());
		if (entity.id != -1) {
			_database.delete(metadata.getTableName(), EntityBase.ID_COLUMN_NAME
					+ "=" + entity.id, null);
		}
	}

	public void deleteAll(Class<? extends EntityBase> entityClass) {
		delete(entityClass, null);
	}

	public void delete(Class<? extends EntityBase> entityClass,
					   String whereClause) {
		EntityMapping metadata = getMetadata(entityClass.getName());
		String query = whereClause == null ? metadata.getDeleteQuery()
				: metadata.getDeleteQuery(whereClause);
		_database.execSQL(query);
	}

	public void deleteByUniqueId(EntityBase entity) {
		final EntityMapping metadata = getMetadata(entity.getClass().getName());
		final ColumnMapping unique = metadata.getUniqueColumnMapping();
		final ContentValues values = fillValues(entity, metadata);
		final String idValue = values.getAsString(unique.fieldName);
		_database.delete(metadata.getTableName(), unique.columnName + "="
				+ idValue, null);
	}

	public void transaction(BatchClass batch) {
		_database.beginTransaction();
		try {
			batch.execInBatch();
			_database.setTransactionSuccessful();
		} finally {
			_database.endTransaction();
		}
	}

	public <T extends EntityBase> ArrayList<T> getEntities(Class<T> entityClass) {
		return getEntities(entityClass, null);
	}

	public <T extends EntityBase> ArrayList<T> getEntities(
			Class<T> entityClass, String whereClause) {
		return getEntities(entityClass, whereClause, null);
	}

	public <T extends EntityBase> ArrayList<T> getEntities(
			Class<T> entityClass, String whereClause, String sortField) {
		return getEntities(entityClass, whereClause, sortField, true);
	}

	public <T extends EntityBase> ArrayList<T> getEntities(
			Class<T> entityClass, String whereClause, String sortField,
			boolean desc) {
		ArrayList<T> result = new ArrayList<T>();
		EntityMapping metadata = getMetadata(entityClass.getName());
		String query = metadata.getSelectQuery(whereClause, sortField, desc);

		Cursor cursor = _database.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			do {
				try {
					T entity = entityClass.newInstance();
					fillObject(entity, metadata, cursor);
					result.add(entity);
				} catch (IllegalAccessException e) {
					Log.e(LOG_TAG, "Can't create entity", e);
				} catch (InstantiationException e) {
					Log.e(LOG_TAG, "Can't create entity", e);
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
	}

	public <T extends EntityBase> T getEntity(Class<T> entityClass,
											  String whereClause) {
		T result = null;
		EntityMapping metadata = getMetadata(entityClass.getName());
		String query = metadata.getSelectQuery(whereClause, null, false);
		Cursor cursor = _database.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			try {
				result = entityClass.newInstance();
				fillObject(result, metadata, cursor);
			} catch (IllegalAccessException e) {
				Log.e(LOG_TAG, "Can't create entity", e);
			} catch (InstantiationException e) {
				Log.e(LOG_TAG, "Can't create entity", e);
			}
		}
		cursor.close();
		return result;
	}

	public <T extends EntityBase> T getEntityById(Class<T> entityClass, int id) {
		T result = null;
		EntityMapping metadata = getMetadata(entityClass.getName());
		String query = metadata.getSelectQuery(
				String.format("%s=%s", EntityBase.ID_COLUMN_NAME, id), null,
				false);
		Cursor cursor = _database.rawQuery(query, null);
		if (cursor.moveToFirst()) {

			try {
				result = entityClass.newInstance();
				fillObject(result, metadata, cursor);
			} catch (IllegalAccessException e) {
				Log.e(LOG_TAG, "Can't create entity", e);
			} catch (InstantiationException e) {
				Log.e(LOG_TAG, "Can't create entity", e);
			}
		}
		cursor.close();
		return result;
	}

	private void createDatabase() {
		String databaseName = String.format("%s.%s", _databaseName,
				_databaseVersion);
		_database = _context.openOrCreateDatabase(databaseName,
				Context.MODE_WORLD_READABLE, null);
		_database.beginTransaction();
		try {
			for (EntityMapping mapping : _entityMappings) {
				String query = mapping.getTableCreationQuery();
				_database.execSQL(query);
			}
			_database.setTransactionSuccessful();
		} finally {
			_database.endTransaction();
			Log.v(LOG_TAG, "Database created");
		}
	}

	private void loadMapping(XmlResourceParser mappingDefinition) {
		EntityMapping entityMapping = null;
		ColumnMapping columnMapping = null;
		try {
			int eventType = mappingDefinition.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String name = null;
				switch (eventType) {
					case XmlPullParser.START_TAG:
						name = mappingDefinition.getName();
						if (name.equals(DATABASE_TAG)) {
							entityMapping = new EntityMapping();
							for (int i = 0; i < mappingDefinition
									.getAttributeCount(); i++) {
								String attribute = mappingDefinition
										.getAttributeName(i);
								if (attribute.equals(NAME_ATTRIBUTE)) {
									_databaseName = mappingDefinition
											.getAttributeValue(i);
								} else if (attribute.equals(VERSION_ATTRIBUTE)) {
									_databaseVersion = Integer
											.parseInt(mappingDefinition
													.getAttributeValue(i));
								}
							}
						}
						if (name.equals(ENTITY_TAG)) {
							entityMapping = new EntityMapping();
							for (int i = 0; i < mappingDefinition
									.getAttributeCount(); i++) {
								String attribute = mappingDefinition
										.getAttributeName(i);
								if (attribute.equals(CLASS_NAME_ATTRIBUTE)) {
									entityMapping.setClassName(mappingDefinition
											.getAttributeValue(i));
								} else if (attribute.equals(TABLE_NAME_ATTRIBUTE)) {
									entityMapping.setTableName(mappingDefinition
											.getAttributeValue(i));
								}
							}
						}
						if (name.equals(MAPPING_TAG)) {
							columnMapping = new ColumnMapping();
							for (int i = 0; i < mappingDefinition
									.getAttributeCount(); i++) {
								String attribute = mappingDefinition
										.getAttributeName(i);
								if (attribute.equals(COLUMN_NAME_ATTRIBUTE)) {
									columnMapping.columnName = mappingDefinition
											.getAttributeValue(i);
								} else if (attribute.equals(FIELD_NAME_ATTRIBUTE)) {
									columnMapping.fieldName = mappingDefinition
											.getAttributeValue(i);
								} else if (attribute.equals(DATA_TYPE_ATTRIBUTE)) {
									columnMapping.dataType = mappingDefinition
											.getAttributeValue(i);
								} else if (attribute.equals(UNIQUE_ATTRIBUTE)) {
									columnMapping.hasUnique = Boolean
											.parseBoolean(mappingDefinition
													.getAttributeValue(i));
								}
							}
						}
						break;
					case XmlPullParser.END_TAG:
						name = mappingDefinition.getName();
						if (name.equals(ENTITY_TAG) && entityMapping != null) {
							_entityMappings.add(entityMapping);
							entityMapping = null;
						}
						if (name.equals(MAPPING_TAG) && entityMapping != null
								&& columnMapping != null) {
							try {
								Field field = Class.forName(
										entityMapping.getClassName()).getField(
										columnMapping.fieldName);
								columnMapping.field = field;
							} catch (SecurityException e) {
								Log.d(LOG_TAG, "Can't get field" + e);
							} catch (NoSuchFieldException e) {
								Log.d(LOG_TAG, "Can't get field" + e);
							} catch (ClassNotFoundException e) {
								Log.d(LOG_TAG, "Can't get field" + e);
							}
							entityMapping.getMappings().add(columnMapping);
							columnMapping = null;
						}
						break;
				}
				eventType = mappingDefinition.next();
			}
		} catch (XmlPullParserException e) {
			throw new RuntimeException("Cannot parse mappings");
		} catch (IOException e) {
			throw new RuntimeException("Cannot parse mappings");
		} finally {
			mappingDefinition.close();
		}
	}

	private EntityMapping getMetadata(String className) {
		for (EntityMapping mapping : _entityMappings) {
			if (mapping.getClassName().equals(className)) {
				return mapping;
			}
		}
		return null;
	}

	private static ContentValues fillValues(EntityBase entity,
											EntityMapping metadata) {
		ContentValues result = new ContentValues();
		ArrayList<ColumnMapping> mappings = metadata.getMappings();
		for (ColumnMapping mapping : mappings) {
			try {
				Field field = mapping.field;
				if (mapping.dataType.equals(INTEGER)) {
					result.put(mapping.columnName, field.getInt(entity));
				} else if (mapping.dataType.equals(TEXT)) {
					result.put(mapping.columnName, (String) field.get(entity));
				} else if (mapping.dataType.equals(REAL)) {
					result.put(mapping.columnName, field.getFloat(entity));
				} else if (mapping.dataType.equals(NUMERIC)) {
					result.put(mapping.columnName, field.getLong(entity));
				} else if (mapping.dataType.equals(BOOLEAN)) {
					result.put(mapping.columnName, field.getBoolean(entity) ? 1
							: 0);
				}
			} catch (SecurityException e) {
				Log.e(LOG_TAG, "Can't fill content values", e);
			} catch (IllegalArgumentException e) {
				Log.e(LOG_TAG, "Can't fill content values", e);
			} catch (IllegalAccessException e) {
				Log.e(LOG_TAG, "Can't fill content values", e);
			}
		}
		return result;
	}

	private static <T extends EntityBase> void fillObject(T entity,
														  EntityMapping metadata, Cursor cursor) {
		ArrayList<ColumnMapping> mappings = metadata.getMappings();
		entity.id = cursor.getInt(cursor
				.getColumnIndex(EntityBase.ID_COLUMN_NAME));
		for (ColumnMapping mapping : mappings) {
			Field field = mapping.field;
			int columnIndex = cursor.getColumnIndex(mapping.columnName);
			try {
				if (mapping.dataType.equals(INTEGER)) {
					field.setInt(entity, cursor.getInt(columnIndex));
				} else if (mapping.dataType.equals(TEXT)) {
					field.set(entity, cursor.getString(columnIndex));
				} else if (mapping.dataType.equals(REAL)) {
					field.setFloat(entity, cursor.getFloat(columnIndex));
				} else if (mapping.dataType.equals(NUMERIC)) {
					field.setLong(entity, cursor.getLong(columnIndex));
				} else if (mapping.dataType.equals(BOOLEAN)) {
					field.setBoolean(entity, cursor.getInt(columnIndex) != 0);
				}
			} catch (IllegalArgumentException e) {
				Log.e(LOG_TAG, "Can't fill object", e);
			} catch (IllegalAccessException e) {
				Log.e(LOG_TAG, "Can't fill object", e);
			}
		}
	}

	public Context getContext() {
		return _context;
	}
}
