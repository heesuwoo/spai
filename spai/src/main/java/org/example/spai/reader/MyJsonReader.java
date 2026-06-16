package org.example.spai.reader;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


@Component
public class MyJsonReader {
    private final Resource resource;
    
	// ETL 파이프라인
	// 데이터를 Extract(추출) -> Transfer(이동=처리) -> Load(적재)
    
    public MyJsonReader(@Value("classpath:data.PageCollection.json") Resource resource) {
        this.resource = resource;
    }

    public List<Document> loadJsonAsDocuments() {
        JsonReader jsonReader = new JsonReader(this.resource, "_id");	// , "_id": 특정한 필드를 명시하면 해당 필드에 대해서만 데이터가 뽑힘
        return jsonReader.get();
    }
}
