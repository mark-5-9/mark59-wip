/*
 *  Copyright 2019 Mark59.com
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mark59.trends.load;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mark59.trends.data.application.dao.ApplicationDAO;
import com.mark59.trends.data.application.dao.ApplicationDAOjdbcTemplateImpl;
import com.mark59.trends.data.eventMapping.dao.EventMappingDAO;
import com.mark59.trends.data.eventMapping.dao.EventMappingDAOjdbcTemplateImpl;
import com.mark59.trends.data.graphMapping.dao.GraphMappingDAO;
import com.mark59.trends.data.graphMapping.dao.GraphMappingDAOjdbcTemplateImpl;
import com.mark59.trends.data.metricSla.dao.MetricSlaDAO;
import com.mark59.trends.data.metricSla.dao.MetricSlaDAOjdbcImpl;
import com.mark59.trends.data.run.dao.RunDAO;
import com.mark59.trends.data.run.dao.RunDAOjdbcTemplateImpl;
import com.mark59.trends.data.sla.dao.SlaDAO;
import com.mark59.trends.data.sla.dao.SlaDAOjdbcImpl;
import com.mark59.trends.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.trends.data.testTransactions.dao.TestTransactionsDAOjdbcTemplateImpl;
import com.mark59.trends.data.transaction.dao.TransactionDAO;
import com.mark59.trends.data.transaction.dao.TransactionDAOjdbcTemplateImpl;

/**
 * Create  Spring bean(s) via program rather than XML configuration<br>
 * <p>For example the applicationDAO method is equivalent to the following appConfig.xml:
 * <pre><code>
 * &lt;bean id="applicationDAO" 	
 *	class="com.mark59.metrics.data.application.dao.ApplicationDAOjdbcTemplateImpl"&gt;
 * &lt;/bean&gt;
 * </code></pre>
 */
@Configuration
public class ApplicationConfig {

    @Value("${spring.profiles.active}")
    private String springProfilesActive;	
	
    @Bean
    String currentDatabaseProfile() {
        return springProfilesActive;
    }   
    
    @Bean
    ApplicationDAO applicationDAO() {
        return new ApplicationDAOjdbcTemplateImpl();
    }
    
    @Bean
    RunDAO runDAO() {
        return new RunDAOjdbcTemplateImpl();
    }
    
    @Bean
    TransactionDAO transactionDAO() {
        return new TransactionDAOjdbcTemplateImpl();
    }
    
    @Bean
    SlaDAO slaDAO() {
        return new SlaDAOjdbcImpl();
    }
    
    @Bean
    MetricSlaDAO metricSlaDAO() {
        return new MetricSlaDAOjdbcImpl();
    }

    @Bean
    GraphMappingDAO graphMappingDAO() {
        return new GraphMappingDAOjdbcTemplateImpl();
    }

    @Bean
    EventMappingDAO eventMappingDAO() {
        return new EventMappingDAOjdbcTemplateImpl();
    }

    @Bean
    TestTransactionsDAO testTransactionsDAO() {
        return new TestTransactionsDAOjdbcTemplateImpl();
    }
    
}