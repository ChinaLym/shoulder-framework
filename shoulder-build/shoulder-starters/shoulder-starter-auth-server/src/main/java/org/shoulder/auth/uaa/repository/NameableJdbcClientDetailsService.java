package org.shoulder.auth.uaa.repository;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;

import javax.sql.DataSource;

/**
 * Basic, JDBC implementation of the client details service.
 * 复制自 spring security，新增了可以修改表名、列名
 *
 * @see JdbcClientDetailsService
 */
public class NameableJdbcClientDetailsService extends JdbcClientDetailsService implements ClientDetailsService,
    ClientRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(NameableJdbcClientDetailsService.class);

    // ----------- super default ---------------------

    private static final String CLIENT_FIELDS_FOR_UPDATE = "resource_ids, scope, "
        + "authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, "
        + "refresh_token_validity, additional_information, autoapprove";

    private static final String CLIENT_FIELDS = "client_secret, " + CLIENT_FIELDS_FOR_UPDATE;

    private static final String BASE_FIND_STATEMENT = "select client_id, " + CLIENT_FIELDS
        + " from oauth_client_details";

    private static final String DEFAULT_FIND_STATEMENT = BASE_FIND_STATEMENT + " order by client_id";

    private static final String DEFAULT_SELECT_STATEMENT = BASE_FIND_STATEMENT + " where client_id = ?";

    private static final String DEFAULT_INSERT_STATEMENT = "insert into oauth_client_details (" + CLIENT_FIELDS
        + ", client_id) values (?,?,?,?,?,?,?,?,?,?,?)";

    private static final String DEFAULT_UPDATE_STATEMENT = "update oauth_client_details " + "set "
        + CLIENT_FIELDS_FOR_UPDATE.replaceAll(", ", "=?, ") + "=? where client_id = ?";

    private static final String DEFAULT_UPDATE_SECRET_STATEMENT = "update oauth_client_details "
        + "set client_secret = ? where client_id = ?";

    private static final String DEFAULT_DELETE_STATEMENT = "delete from oauth_client_details where client_id = ?";

    // ----------- super default ---------------------

    public NameableJdbcClientDetailsService(DataSource dataSource, PasswordEncoder passwordEncoder) {
        super(dataSource);
        setPasswordEncoder(passwordEncoder);
    }

    public void setTableName(String newTableName) {
        String oldTableName = "oauth_client_details";
        setInsertClientDetailsSql(DEFAULT_INSERT_STATEMENT.replace(oldTableName, newTableName));
        setDeleteClientDetailsSql(DEFAULT_DELETE_STATEMENT.replace(oldTableName, newTableName));
        setUpdateClientDetailsSql(DEFAULT_UPDATE_STATEMENT.replace(oldTableName, newTableName));
        setUpdateClientSecretSql(DEFAULT_UPDATE_SECRET_STATEMENT.replace(oldTableName, newTableName));
        setSelectClientDetailsSql(DEFAULT_SELECT_STATEMENT.replace(oldTableName, newTableName));
        // findAll
        setFindClientDetailsSql(DEFAULT_FIND_STATEMENT.replace(oldTableName, newTableName));
        log.warn("change table name from {} to {}", oldTableName, newTableName);
    }

}
