package org.example.spai.config;

import org.elasticsearch.client.RestClient;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.SimilarityFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {
	private final OpenAiEmbeddingModel openAiEmbeddingModel;
	
	// 생성자 방식으로 객체 주입
	public VectorStoreConfig(OpenAiEmbeddingModel openAiEmbeddingModel) {
		this.openAiEmbeddingModel = openAiEmbeddingModel;
	}



	@Bean
	public VectorStore elasticsearchVectorStore(RestClient restClient) {
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName("yummi-docs");	// 인덱스 생성
        options.setSimilarity(SimilarityFunction.cosine);
        options.setDimensions(1536);

        return ElasticsearchVectorStore.builder(restClient, openAiEmbeddingModel)
                .options(options)
                .initializeSchema(true)	// 특정한 인덱스를 자동으로 생성시킬건지, (JPA의 DDL = auto 와 비슷)
                .batchingStrategy(new TokenCountBatchingStrategy())
                .build();		
	}
	
}
