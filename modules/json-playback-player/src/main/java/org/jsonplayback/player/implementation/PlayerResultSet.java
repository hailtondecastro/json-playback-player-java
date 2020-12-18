package org.jsonplayback.player.implementation;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerResultSet implements ResultSet {
	private Object[] internalValuesArr = null;
	private Map<String, Object> internalValuesByNameMap = new LinkedHashMap<>();
	
	public PlayerResultSet(Object[] internalValuesArr) {
		this.internalValuesArr = internalValuesArr;
		for (int i = 0; i < internalValuesArr.length; i++) {
			this.internalValuesByNameMap.put("" + i, this.internalValuesArr[i]);
		}
	}
	
	public String[] getColumnNames() {
		return new ArrayList<String>(this.internalValuesByNameMap.keySet()).toArray(new String[this.internalValuesByNameMap.size()]);
	}
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.unwrap(iface))*/
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.isWrapperFor(iface))*/
	}

	@Override
	public boolean next() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.next())*/
	}

	@Override
	public void close() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.close())*/
	}

	@Override
	public boolean wasNull() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (String)this.internalValuesArr[columnIndex];
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Boolean)this.internalValuesArr[columnIndex];
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Byte)this.internalValuesArr[columnIndex];
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Short)this.internalValuesArr[columnIndex];
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (int)this.internalValuesArr[columnIndex];
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Long)this.internalValuesArr[columnIndex];
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Float)this.internalValuesArr[columnIndex];
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Double)this.internalValuesArr[columnIndex];
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		// TODO Auto-generated method stub
		return (BigDecimal)this.internalValuesArr[columnIndex];
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (byte[])this.internalValuesArr[columnIndex];
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Date)this.internalValuesArr[columnIndex];
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Time)this.internalValuesArr[columnIndex];
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Timestamp)this.internalValuesArr[columnIndex];
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (InputStream)this.internalValuesArr[columnIndex];
	}

	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (InputStream)this.internalValuesArr[columnIndex];
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (InputStream)this.internalValuesArr[columnIndex];
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (String)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Boolean)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Byte)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Short)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (int)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (long)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (float)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Double)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		// TODO Auto-generated method stub
		return (BigDecimal)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (byte[])this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Date)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Time)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Timestamp)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (InputStream)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (InputStream)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (InputStream)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.getWarnings())*/
	}

	@Override
	public void clearWarnings() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.clearWarnings())*/
	}

	@Override
	public String getCursorName() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.getCursorName())*/
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.getMetaData())*/
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Object)this.internalValuesArr[columnIndex];
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Object)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.findColumn(columnLabel))*/
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Reader)this.internalValuesArr[columnIndex];
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Reader)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (BigDecimal)this.internalValuesArr[columnIndex];
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (BigDecimal)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.isBeforeFirst())*/
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.isAfterLast())*/
	}

	@Override
	public boolean isFirst() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.isFirst())*/
	}

	@Override
	public boolean isLast() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.isLast())*/
	}

	@Override
	public void beforeFirst() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.beforeFirst())*/
	}

	@Override
	public void afterLast() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.afterLast())*/
	}

	@Override
	public boolean first() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.first())*/
	}

	@Override
	public boolean last() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.last())*/
	}

	@Override
	public int getRow() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.getRow())*/
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.absolute(row))*/
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.relative(rows))*/
	}

	@Override
	public boolean previous() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.previous())*/
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.setFetchDirection(direction))*/
	}

	@Override
	public int getFetchDirection() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.getFetchDirection())*/
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.setFetchSize(rows))*/
	}

	@Override
	public int getFetchSize() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.getFetchSize())*/
	}

	@Override
	public int getType() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.getType())*/
	}

	@Override
	public int getConcurrency() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.getConcurrency())*/
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.rowUpdated())*/
	}

	@Override
	public boolean rowInserted() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.rowInserted())*/
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.rowDeleted())*/
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNull(columnIndex))*/
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBoolean(columnIndex, x))*/
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateByte(columnIndex, x))*/
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateShort(columnIndex, x))*/
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateInt(columnIndex, x))*/
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateLong(columnIndex, x))*/
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateFloat(columnIndex, x))*/
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateDouble(columnIndex, x))*/
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBigDecimal(columnIndex, x))*/
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateString(columnIndex, x))*/
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBytes(columnIndex, x))*/
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateDate(columnIndex, x))*/
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateTime(columnIndex, x))*/
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateTimestamp(columnIndex, x))*/
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateAsciiStream(columnIndex, x, length))*/
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBinaryStream(columnIndex, x, length))*/
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateCharacterStream(columnIndex, x, length))*/
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateObject(columnIndex, x, scaleOrLength))*/
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateObject(columnIndex, x))*/
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNull(columnLabel))*/
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBoolean(columnLabel, x))*/
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateByte(columnLabel, x))*/
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateShort(columnLabel, x))*/
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateInt(columnLabel, x))*/
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateLong(columnLabel, x))*/
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateFloat(columnLabel, x))*/
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateDouble(columnLabel, x))*/
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBigDecimal(columnLabel, x))*/
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateString(columnLabel, x))*/
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBytes(columnLabel, x))*/
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateDate(columnLabel, x))*/
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateTime(columnLabel, x))*/
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateTimestamp(columnLabel, x))*/
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateAsciiStream(columnLabel, x, length))*/
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBinaryStream(columnLabel, x, length))*/
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateCharacterStream(columnLabel, reader, length))*/
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateObject(columnLabel, x, scaleOrLength))*/
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateObject(columnLabel, x))*/
	}

	@Override
	public void insertRow() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.insertRow())*/
	}

	@Override
	public void updateRow() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateRow())*/
	}

	@Override
	public void deleteRow() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.deleteRow())*/
	}

	@Override
	public void refreshRow() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.refreshRow())*/
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.cancelRowUpdates())*/
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.moveToInsertRow())*/
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.moveToCurrentRow())*/
	}

	@Override
	public Statement getStatement() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.getStatement())*/
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		// TODO Auto-generated method stub
		return (Object)this.internalValuesArr[columnIndex];
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Ref)this.internalValuesArr[columnIndex];
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Blob)this.internalValuesArr[columnIndex];
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Clob)this.internalValuesArr[columnIndex];
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Array)this.internalValuesArr[columnIndex];
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		// TODO Auto-generated method stub
		return (Object)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Ref)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Blob)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Clob)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Array)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		// TODO Auto-generated method stub
		return (Date)this.internalValuesArr[columnIndex];
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		// TODO Auto-generated method stub
		return (Date)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		// TODO Auto-generated method stub
		return (Time)this.internalValuesArr[columnIndex];
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		// TODO Auto-generated method stub
		return (Time)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		// TODO Auto-generated method stub
		return (Timestamp)this.internalValuesArr[columnIndex];
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		// TODO Auto-generated method stub
		return (Timestamp)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (URL)this.internalValuesArr[columnIndex];
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (URL)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateRef(columnIndex, x))*/
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateRef(columnLabel, x))*/
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBlob(columnIndex, x))*/
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBlob(columnLabel, x))*/
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateClob(columnIndex, x))*/
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateClob(columnLabel, x))*/
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateArray(columnIndex, x))*/
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateArray(columnLabel, x))*/
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (RowId)this.internalValuesArr[columnIndex];
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (RowId)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateRowId(columnIndex, x))*/
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateRowId(columnLabel, x))*/
	}

	@Override
	public int getHoldability() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.getHoldability())*/
	}

	@Override
	public boolean isClosed() throws SQLException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException();/*#(return super.isClosed())*/
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNString(columnIndex, nString))*/
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNString(columnLabel, nString))*/
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNClob(columnIndex, nClob))*/
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNClob(columnLabel, nClob))*/
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (NClob)this.internalValuesArr[columnIndex];
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (NClob)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (SQLXML)this.internalValuesArr[columnIndex];
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (SQLXML)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateSQLXML(columnIndex, xmlObject))*/
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateSQLXML(columnLabel, xmlObject))*/
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (String)this.internalValuesArr[columnIndex];
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (String)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return (Reader)this.internalValuesArr[columnIndex];
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
		return (Reader)this.internalValuesByNameMap.get(columnLabel);	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNCharacterStream(columnIndex, x, length))*/
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNCharacterStream(columnLabel, reader, length))*/
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateAsciiStream(columnIndex, x, length))*/
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBinaryStream(columnIndex, x, length))*/
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateCharacterStream(columnIndex, x, length))*/
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateAsciiStream(columnLabel, x, length))*/
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBinaryStream(columnLabel, x, length))*/
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateCharacterStream(columnLabel, reader, length))*/
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBlob(columnIndex, inputStream, length))*/
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBlob(columnLabel, inputStream, length))*/
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateClob(columnIndex, reader, length))*/
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateClob(columnLabel, reader, length))*/
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNClob(columnIndex, reader, length))*/
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNClob(columnLabel, reader, length))*/
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNCharacterStream(columnIndex, x))*/
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNCharacterStream(columnLabel, reader))*/
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateAsciiStream(columnIndex, x))*/
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBinaryStream(columnIndex, x))*/
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateCharacterStream(columnIndex, x))*/
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateAsciiStream(columnLabel, x))*/
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBinaryStream(columnLabel, x))*/
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateCharacterStream(columnLabel, reader))*/
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBlob(columnIndex, inputStream))*/
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateBlob(columnLabel, inputStream))*/
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateClob(columnIndex, reader))*/
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateClob(columnLabel, reader))*/
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNClob(columnIndex, reader))*/
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();/*#(super.updateNClob(columnLabel, reader))*/
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		// TODO Auto-generated method stub
		return (T)this.internalValuesArr[columnIndex];
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		// TODO Auto-generated method stub
		return (T)this.internalValuesByNameMap.get(columnLabel);	
	}

}
