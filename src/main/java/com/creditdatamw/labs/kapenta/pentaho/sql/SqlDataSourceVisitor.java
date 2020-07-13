package com.creditdatamw.labs.kapenta.pentaho.sql;

import com.zaxxer.hikari.HikariConfig;
import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.CompoundDataFactory;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SimpleSQLReportDataFactory;
import org.pentaho.reporting.engine.classic.core.util.AbstractStructureVisitor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Change the DataSource defined in a report at runtime by using this class.
 * Calling the {@link SqlDataSourceVisitor}'s <code>process()</code>
 * method will cause the report's dataSource to be changed to the dataSource
 * passed in the constructor.
 * 
 * The original file is not manipulated but a copy is manipulated and returned
 * in order to keep the original intact - i.e. no side effects on the original copy
 *
 *
 * @author Zikani Nyirenda Mwase <zikani@creditdatamw.com>
 */
public class SqlDataSourceVisitor extends AbstractStructureVisitor {
    private final AtomicReference<ConnectionProvider> connectionProvider;

    public SqlDataSourceVisitor(String databaseUrl) {
        this.connectionProvider = new AtomicReference<>(new ConnectionProviderImpl(databaseUrl));
    }

    public void visit(AbstractReportDefinition reportDefinition) {
        inspect(reportDefinition);
    }

    @Override
    protected void inspect(AbstractReportDefinition reportDefinition) {
        modifyDataSources(reportDefinition);
        super.inspect(reportDefinition);
    }
        
    protected void modifyDataSources(AbstractReportDefinition report) {
        CompoundDataFactory dataFactory = CompoundDataFactory.normalize(report.getDataFactory());
        
        final int size = dataFactory.size();
        
        for(int i = 0; i < size; i++) {
            dataFactory.set(i, modifyDataSource(dataFactory.getReference(i)));
        }
        report.setDataFactory(dataFactory);
    }
    
    protected DataFactory modifyDataSource(DataFactory dataFactory) {
        // We are only concerned with SQL Data Factories at the moment
        if(dataFactory instanceof SimpleSQLReportDataFactory) {
            SimpleSQLReportDataFactory sqlDataFactory = (SimpleSQLReportDataFactory) dataFactory;

            sqlDataFactory.setConnectionProvider(connectionProvider.get());
            
            return sqlDataFactory;
        }

        return dataFactory;
    }
    
    private static final class ConnectionProviderImpl implements ConnectionProvider {
        private static final long serialVersionUID = 1L;

        private transient HikariConfig hikariConfig;

        ConnectionProviderImpl(String databaseUrl) {
            this.hikariConfig = new HikariConfig();
            this.hikariConfig.setJdbcUrl(databaseUrl);
        }

        @Override
        public Object getConnectionHash() {
            return Objects.hash(hikariConfig.getDataSourceClassName());
        }
        
        @Override
        public Connection createConnection(String user, String password)
                throws SQLException {
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(password);
            return hikariConfig.getDataSource().getConnection();
        }
    }
}
