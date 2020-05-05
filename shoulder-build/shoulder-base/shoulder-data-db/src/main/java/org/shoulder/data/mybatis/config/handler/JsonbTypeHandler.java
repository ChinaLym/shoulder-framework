package org.shoulder.data.mybatis.config.handler;

/*
@MappedTypes(Object.class)
public class JsonbTypeHandler extends BaseTypeHandler<Object> {

    private static Logger log = LoggerFactory.getLogger(JsonbTypeHandler.class);

    private static final String EXTENDED_ATTRIBUTE = "attribute_extended";

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int index, Object parameter, JdbcType jdbcType) throws SQLException {
        if (parameter instanceof Integer) {
            preparedStatement.setInt(index,((Integer) parameter).intValue());
        } else if (parameter instanceof String) {
            preparedStatement.setString(index,parameter.toString());
        } else if (parameter instanceof Date) {
            Timestamp ts = new Timestamp(((Date) parameter).getTime());
///         java.sql.Date sqlDate = new java.sql.Date(.getTime());
            preparedStatement.setTimestamp(index,ts);
        } else if (parameter instanceof Map){
            PGobject pgObj = new PGobject();
            pgObj.setType("jsonb");
            try {
                pgObj.setValue(JsonUtils.toJsonString(parameter));
            } catch (JsonProcessingException e) {
                log.error("JsonbTypeHandler handle json error");
            }
            preparedStatement.setObject(index,pgObj);
        } else if(parameter instanceof  String[] || parameter instanceof  Object[]){
            Connection conn = preparedStatement.getConnection();
            Array array = conn.createArrayOf("text", (Object[]) parameter);
            preparedStatement.setArray(index, array);
        }else if (parameter instanceof List){
            String[] paramas = (String[]) ((List) parameter).toArray();
            Connection conn = preparedStatement.getConnection();
            Array array = conn.createArrayOf("text",  paramas);
            preparedStatement.setArray(index, array);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        if(EXTENDED_ATTRIBUTE.equals(columnName)){
            String res =  rs.getString(columnName);
            return convert2Map(res);
        }else {
            return rs.getString(columnName);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String res =  rs.getString(columnIndex);
        return convert2Map(res);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String res =  cs.getString(columnIndex);
        return convert2Map(res);
    }

    private Object convert2Map(String res){
        if(null!=res) {
            TypeReference<HashMap> type = new TypeReference<HashMap>() {};
            try {
                return JsonUtils.parseObject(res, type);
            } catch (IOException e) {
                log.error("JsonbTypeHandler handle json error");
            }
        }
        return new HashMap();
    }

}
*/