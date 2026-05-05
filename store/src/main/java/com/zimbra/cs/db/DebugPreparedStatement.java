// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
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
import java.util.List;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.util.Zimbra;

class DebugPreparedStatement implements PreparedStatement {

    private static final int MAX_STRING_LENGTH = 1024;
    private static long sSlowSqlThreshold = Long.MAX_VALUE;
    
    private final PreparedStatement mStmt;
    private String mSql;
    private long mStartTime;

    /**
     * A list that implicitly resizes when {@link #set} is called.
     */
    @SuppressWarnings("serial")
    private static class AutoSizeList<E>
    extends ArrayList<E> {
        public E set(int index, E element) {
            if (index >= size()) {
                for (int i = size(); i <= index; i++) {
                    add(null);
                }
            }
            return super.set(index, element);
        }
    }
    private List<Object> mParams = new AutoSizeList<>();
    
    DebugPreparedStatement(DebugConnection conn, PreparedStatement stmt, String sql) {
        mStmt = stmt;
        mSql = sql;
    }

    public static void setSlowSqlThreshold(long millis) {
        ZimbraLog.sqltrace.info("Setting slow SQL threshold to %dms.", millis);
        sSlowSqlThreshold = millis;
    }

    private String getSql() {
        if (mSql == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        int start = 0;
        int qPos = mSql.indexOf('?', start);
        int paramIndex = 1;
        
        while (qPos >= 0) {
            buf.append(mSql, start, qPos);
            if (paramIndex == mParams.size()) {
                throw new IllegalStateException("Not enough parameters bound for SQL: " + mSql);
            }
            Object o = mParams.get(paramIndex);
            if (o == null) {
                o = "NULL";
            } else if (o instanceof String) {
                // Escape single-quotes
                String s = (String) o;
                if (s.indexOf('\'') >= 0) {
                    s = s.replaceAll("'", "''");
                }
                o = "'" + s + "'";
            }
            buf.append(o);
            
            // Increment indexes
            start = qPos + 1;
            if (start >= mSql.length()) {
                break;
            }
            qPos = mSql.indexOf('?', start);
            paramIndex++;
        }
        
        if (start < mSql.length()) {
            // Append the rest of the string
            buf.append(mSql.substring(start));
        }
        return buf.toString();
    }
    
    private void log() {
        long time = System.currentTimeMillis() - mStartTime;
        if (time > sSlowSqlThreshold) {
            String sql = getSql();
            ZimbraLog.sqltrace.info("Slow execution (%dms): %s", time,  sql);
        } else if (ZimbraLog.sqltrace.isDebugEnabled()) {
            String sql = getSql();
            ZimbraLog.sqltrace.debug(sql + " - " + time + "ms" + getHashCodeString());
        }
    }
    
    private void logException(SQLException e) {
        if (ZimbraLog.sqltrace.isDebugEnabled()) {
            ZimbraLog.sqltrace.debug(e.toString() + ": " + getSql() + getHashCodeString());
        }
    }
    
    private void processDbError(SQLException e) {
        if (Db.errorMatches(e, Db.Error.TABLE_FULL))
            Zimbra.halt("DB out of space", e);
    }

    private String getHashCodeString() {
        String hashCodeString = "";
        try {
            hashCodeString= ", conn=" + mStmt.getConnection().hashCode();
        } catch (SQLException e) {
            ZimbraLog.sqltrace.warn("Unable to determine connection hashcode", e);
        }
        return hashCodeString;
    }
    
    private void startTimer() {
        mStartTime = System.currentTimeMillis();
    }
    
    /////////// PreparedStatement implementation ///////////////

    public ResultSet executeQuery() throws SQLException {
        startTimer();
        ResultSet rs;
        try {
            rs = mStmt.executeQuery();
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return rs;
    }

    public int executeUpdate() throws SQLException {
        startTimer();
        int numRows;
        try {
            numRows = mStmt.executeUpdate();
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return numRows;
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        mParams.set(parameterIndex, null);
        mStmt.setNull(parameterIndex, sqlType);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setBoolean(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setByte(parameterIndex, x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setShort(parameterIndex, x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setInt(parameterIndex, x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setLong(parameterIndex, x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setFloat(parameterIndex, x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setDouble(parameterIndex, x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setBigDecimal(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        String loggedValue = x;
        if (x != null && x.length() > MAX_STRING_LENGTH) {
            loggedValue = loggedValue.substring(0, MAX_STRING_LENGTH) + "...";
        }
        mParams.set(parameterIndex, loggedValue);
        mStmt.setString(parameterIndex, x);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        mParams.set(parameterIndex, "<byte[]>");
        mStmt.setBytes(parameterIndex, x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setDate(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setTime(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setTimestamp(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
    throws SQLException {
        mParams.set(parameterIndex, "<Ascii Stream>");
        mStmt.setAsciiStream(parameterIndex, x, length);
    }

    @SuppressWarnings("deprecation")
    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
    throws SQLException {
        mParams.set(parameterIndex, "<Unicode Stream>");
        mStmt.setUnicodeStream(parameterIndex, x, length);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length)
    throws SQLException {
        mParams.set(parameterIndex, "<Binary Stream>");
        mStmt.setBinaryStream(parameterIndex, x, length);
    }

    public void clearParameters() throws SQLException {
        mParams.clear();
        mStmt.clearParameters();
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
    throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setObject(parameterIndex, x, targetSqlType, scale);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
    throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setObject(parameterIndex, x, targetSqlType);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setObject(parameterIndex, x);
    }

    public boolean execute() throws SQLException {
        startTimer();
        boolean result;
        try {
            result = mStmt.execute();
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return result;
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length)
    throws SQLException {
        mParams.set(parameterIndex, "<Character Stream>");
        mStmt.setCharacterStream(parameterIndex, reader, length);
    }

    public void setRef(int i, Ref x) throws SQLException {
        mParams.set(i, "<Ref>");
        mStmt.setRef(i, x);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        mParams.set(i, "<Blob>");
        mStmt.setBlob(i, x);
    }

    public void setClob(int i, Clob x) throws SQLException {
        mParams.set(i, "<Clob>");
        mStmt.setClob(i, x);
    }

    public void setArray(int i, Array x) throws SQLException {
        mParams.set(i, "<Array>");
        mStmt.setArray(i, x);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setDate(parameterIndex, x, cal);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setTime(parameterIndex, x, cal);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setTimestamp(parameterIndex, x, cal);
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        mParams.set(paramIndex, null);
        mStmt.setNull(paramIndex, sqlType, typeName);
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        mParams.set(parameterIndex, x);
        mStmt.setURL(parameterIndex, x);
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        mSql = sql;
        startTimer();
        ResultSet rs;
        try {
            rs = mStmt.executeQuery(sql);
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return rs;
    }

    public int executeUpdate(String sql) throws SQLException {
        mSql = sql;
        startTimer();
        int numRows = 0;
        try {
            mStmt.executeUpdate(sql);
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return numRows;
    }

    public boolean execute(String sql) throws SQLException {
        mSql = sql;
        startTimer();
        boolean result = false;
        try {
            mStmt.execute(sql);
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return result;
    }

    public int[] executeBatch() throws SQLException {
        startTimer();
        int[] result;
        try {
            result = mStmt.executeBatch();
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return result;
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        startTimer();
        int numRows;
        try {
            numRows = mStmt.executeUpdate(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return numRows;
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        startTimer();
        int numRows;
        try {
            numRows = mStmt.executeUpdate(sql, columnIndexes);
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return numRows;
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        startTimer();
        int numRows;
        try {
            numRows = mStmt.executeUpdate(sql, columnNames);
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return numRows;
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        startTimer();
        boolean result;
        try {
            result = mStmt.execute(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return result;
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        startTimer();
        boolean result;
        try {
            result = mStmt.execute(sql, columnIndexes);
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return result;
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        startTimer();
        boolean result;
        try {
            result = mStmt.execute(sql, columnNames);
        } catch (SQLException e) {
            logException(e);
            processDbError(e);
            throw e;
        }
        log();
        return result;
    }

    // Delegate all other PreparedStatement interface methods to mStmt
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return mStmt.getMetaData();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return mStmt.getParameterMetaData();
    }

    @Override
    public void addBatch() throws SQLException {
        mStmt.addBatch();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        mStmt.setRowId(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        mStmt.setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        mStmt.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        mStmt.setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        mStmt.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        mStmt.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        mStmt.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        mStmt.setSQLXML(parameterIndex, xmlObject);
    }



    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        mStmt.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        mStmt.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        mStmt.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        mStmt.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        mStmt.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        mStmt.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        mStmt.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        mStmt.setClob(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        mStmt.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        mStmt.setNClob(parameterIndex, reader);
    }

    // Statement interface methods
    
    @Override
    public int getMaxFieldSize() throws SQLException {
        return mStmt.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        mStmt.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return mStmt.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        mStmt.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        mStmt.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return mStmt.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        mStmt.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        mStmt.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return mStmt.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        mStmt.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        mStmt.setCursorName(name);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return mStmt.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return mStmt.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return mStmt.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        mStmt.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return mStmt.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        mStmt.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return mStmt.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return mStmt.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return mStmt.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        mStmt.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        mStmt.clearBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return mStmt.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return mStmt.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return mStmt.getGeneratedKeys();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return mStmt.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return mStmt.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        mStmt.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return mStmt.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        mStmt.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return mStmt.isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return mStmt.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return mStmt.isWrapperFor(iface);
    }

    @Override
    public void close() throws SQLException {
        mStmt.close();
    }
}
