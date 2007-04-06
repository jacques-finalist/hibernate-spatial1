package com.cadrie.hibernate.spatial.postgis.test;

import com.cadrie.hibernate.spatial.test.model.DataGenerator;

public class GenerateData {

    public static void main(String[] args) {
	DataGenerator generator = new DataGenerator();
	generator.generate();
    }

}
