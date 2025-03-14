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

package com.mark59.datahunter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.data.policies.dao.PoliciesDAOjdbcTemplateImpl;

/**
 * Create  Spring bean(s) via program rather than XML configuration<br>
 */
@Configuration
public class ApplicationConfig {

    @Value("${spring.profiles.active}")
    private String springProfilesActive;

    @Bean
    String currentDatabaseProfile() {
        return springProfilesActive;
    }


    /**
     * This method is equivalent to the following appConfig.xml:
     * <pre><code>
     * &lt;bean id="PoliciesDAO" 	
     *	class="com.mark59.datahunter.data.policies.dao.PoliciesDAOjdbcTemplateImpl"&gt;
     * &lt;/bean&gt;
     * </code></pre>
     */
    @Bean
    PoliciesDAO policiesDAO() {
        return new PoliciesDAOjdbcTemplateImpl();
    }
}