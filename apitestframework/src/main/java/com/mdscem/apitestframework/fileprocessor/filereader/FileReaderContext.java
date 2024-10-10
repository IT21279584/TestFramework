    package com.mdscem.apitestframework.fileprocessor.filereader;

    import com.fasterxml.jackson.databind.JsonNode;

    public class FileReaderContext {

        private IFileReader iFileReader;

        public FileReaderContext(IFileReader iFileReader){
            this.iFileReader = iFileReader;
        }

        public String loadTestCases(String content){
            return iFileReader.readTestCases(content);
        }
    }
