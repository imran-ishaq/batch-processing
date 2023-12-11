package com.example.batchprocessing.Config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "com.example.batchprocessing.Config",
        mongoTemplateRef = "primaryMongoTemplate"
)
public class primaryDbConfig {

    @Primary
    @Bean(name = "primaryProperties")
    @ConfigurationProperties(prefix = "mongodb.primary")
    public MongoProperties primaryProperties() {
        return new MongoProperties();
    }

    @Bean(name = "primaryClient")
    public MongoClient mongoClient(@Qualifier("primaryProperties") MongoProperties mongoProperties) {
        return MongoClients.create(mongoProperties.getUri());
    }

    @Bean(name = "primaryDbFactory")
    public MongoDatabaseFactory mongoDatabaseFactory(@Qualifier("primaryClient") MongoClient mongoClient,
                                                     @Qualifier("primaryProperties") MongoProperties mongoProperties) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, mongoProperties.getDatabase());
    }

    @Bean(name = "primaryTemplate")
    public MongoTemplate mongoTemplate(@Qualifier("primaryDbFactory") MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}