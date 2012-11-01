package com.dotcypress.database;

import java.util.ArrayList;

public class EntityMapping {

	private String _className;
	private String _tableName;
	private ArrayList<ColumnMapping> _mappings = new ArrayList<ColumnMapping>();

	public String getTableCreationQuery() {
		String columnsDefinition = "";
		String dataType = null;
		for (ColumnMapping mapping : _mappings) {
			dataType = mapping.dataType;
			if (dataType == DatabaseDispatcher.BOOLEAN) {
				dataType = DatabaseDispatcher.INTEGER;
			}
			columnsDefinition += String.format(", %s %s %s", mapping.columnName, dataType, mapping.hasUnique ? "UNIQUE" : "");
		}
		return String.format("CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY AUTOINCREMENT %s)", _tableName, EntityBase.ID_COLUMN_NAME, columnsDefinition);
	}

	public ColumnMapping getUniqueColumnMapping() {
		for (ColumnMapping mapping : _mappings) {
			if (mapping.hasUnique)
				return mapping;
		}
		return null;
	}

	public String getCountQuery() {
		return String.format("select count(%s) from %s", EntityBase.ID_COLUMN_NAME, getTableName());
	}

	public String getCountQuery(String whereClause) {
		return String.format("select count(%s) from %s where %s", EntityBase.ID_COLUMN_NAME, getTableName(), whereClause);
	}

	public String getSelectQuery(String whereClause, String sortField, boolean desc) {
		String query = "select * from " + getTableName();
		if (whereClause != null) {
			query += " where " + whereClause;
		}
		if (sortField != null) {
			query += " ORDER BY " + sortField + (desc ? " DESC" : " ASC");
		}
		return query;
	}

	public void setTableName(String tableName) {
		_tableName = tableName;
	}

	public String getTableName() {
		return _tableName;
	}

	public ArrayList<ColumnMapping> getMappings() {
		return _mappings;
	}

	public void setClassName(String className) {
		_className = className;
	}

	public String getClassName() {
		return _className;
	}

	public String getDeleteQuery() {
		return String.format("delete from %s", getTableName());
	}

	public String getDeleteQuery(String whereClause) {
		return String.format("delete from %s where %s", getTableName(), whereClause);
	}

}
