package org.shoulder.data.mybatis.config.handler;

/**
 * @author lym
 */
/*@MappedTypes(List.class)

public class ListTypeHandler extends BaseTypeHandler<List> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List parameter, JdbcType jdbcType) throws SQLException {
        PGobject pgObj = new PGobject();
        pgObj.setType("text[]");
        pgObj.setValue("{"+org.apache.commons.lang3.StringUtils.join(parameter,",")+"}");
        ps.setObject(i,pgObj);
    }

    @Override
    public List getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if(null != rs.getString(columnName)) {
            Array array = rs.getArray(columnName);
            // 多查询几次 {encodeDevice} 会变成 {"encodeDevice"}
            String result = rs.getString(columnName).replaceAll("\\{","").replaceAll("}","").replaceAll("\"","");
            return new ArrayList(Arrays.asList(result.split(",")));
        }
        return new ArrayList();
    }

    @Override
    public List getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if(null != rs.getString(columnIndex)) {
            return new ArrayList(Arrays.asList(rs.getString(columnIndex).split(",")));
        }
        return new ArrayList();
    }

    @Override
    public List getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if(null!=cs.getString(columnIndex)) {
            return new ArrayList(Arrays.asList(cs.getString(columnIndex).split(",")));
        }
        return new ArrayList();
    }
}
*/
