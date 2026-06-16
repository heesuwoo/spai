package org.example.spai.api;

import java.util.List;

import org.example.spai.reader.MyJsonReader;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ReaderController {

	private final MyJsonReader myJsonReader;
	
	public ReaderController(MyJsonReader myJsonReader) {
		this.myJsonReader = myJsonReader;
	}
	
	@GetMapping("/reader")
	public List<Document> reader() {
		return myJsonReader.loadJsonAsDocuments();
	}
	
}
