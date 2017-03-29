package com.creditdatamw.labs.sparkpentaho.reports;

import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.CompoundDataFactory;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SimpleSQLReportDataFactory;
import org.pentaho.reporting.engine.classic.core.util.AbstractStructureVisitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Change the DataSource defined in a report at runtime by using this class.
 * Calling the {@link SQLDataSourceVisitor}'s <code>process()</code>
 * method will cause the report's dataSource to be changed to the dataSource
 * passed in the constructor.
 * 
 * The original file is not manipulated but a copy is manipulated and returned
 * in order to keep the original intact - i.e. no side effects on the original copy
 *
 *
 * @author Zikani
 */
public class SQLDataSourceVisitor extends AbstractStructureVisitor {
    private final ConnectionProvider connectionProvider;

    public SQLDataSourceVisitor(String databaseUrl, String user, String password) {
        this.connectionProvider = new ConnectionProviderImpl(databaseUrl, user, password);
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

            sqlDataFactory.setConnectionProvider(connectionProvider);
            
            return sqlDataFactory;
        }
        
        return dataFactory;
    }
    
    private static final class ConnectionProviderImpl implements ConnectionProvider {
        private static final long serialVersionUID = 1L;

        final String databaseUrl, user, password;
        
        public ConnectionProviderImpl(String databaseUrl, String user, String password) {
            this.databaseUrl = databaseUrl;
            this.user = user;
            this.password = password;
        }
        
        @Override
        public Object getConnectionHash() {
            return Objects.hashCode(databaseUrl);
        }
        
        @Override
        public Connection createConnection(String user, String password)
                throws SQLException {
            // Uses details provided in the constructor - arguments ignored
            return DriverManager.getConnection(this.databaseUrl, this.user, this.password);
        }
    }
}
