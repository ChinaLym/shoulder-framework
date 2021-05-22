package org.shoulder.data.mybatis.interceptor.typehandler;

import com.baomidou.mybatisplus.core.enums.SqlLike;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * base like查询
 *
 * @author lym
 */
public class BaseLikeTypeHandler extends BaseTypeHandler<CharSequence> {

    private final SqlLike likeType;

    public BaseLikeTypeHandler(final SqlLike likeType) {
        this.likeType = likeType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CharSequence parameter, JdbcType jdbcType)
            throws SQLException {
        if (parameter == null) {
            ps.setString(i, null);
        } else {
            ps.setString(i, like(parameter.toString()));
        }
    }

    private String like(String parameter) {
        return SqlUtils.concatLike(parameter, likeType);
    }


    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getString(columnIndex);
    }

}
