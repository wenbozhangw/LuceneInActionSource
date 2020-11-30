package com.action.listing;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author ：wenbo.zhangw
 * @version :
 * @date ：Created in 2020/11/30 3:24 下午
 * @description ：Listing 1.1  Indexer which indexes .txt files
 * @modified By ：
 */
public class Indexer {

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java " + Indexer.class.getName() + " <index dir> <data dir>");
        }
        // Create index in this directory
        String indexDir = args[0];
        // Index *.txt files from this directory
        String dataDir = args[1];

        long start = System.currentTimeMillis();
        Indexer indexer = new Indexer(indexDir);
        int numIndexed;
        try {
            numIndexed = indexer.index(dataDir, new TextFilesFilter());
        } finally {
            indexer.close();
        }
        long end = System.currentTimeMillis();

        System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
    }

    private IndexWriter writer;

    public Indexer(String indexDir) throws IOException {
        Directory dir = FSDirectory.open(new File(indexDir));
        // Create Lucene IndexWriter
        writer = new IndexWriter(dir,
                new StandardAnalyzer(Version.LUCENE_30),
                true,
                IndexWirter.MaxFieldLength.UNLIMITED);
    }

    public void close() throws IOException {
        // Close IndexWriter
        writer.close();
    }

    public int index(String dataDir, FileFilter filter) throws IOException {
        File[] files = new File(dataDir).listFiles();

        for (File f : files) {
            if (!f.isDirectory() &&
                    !f.isHidden() &&
                    !f.exists() &&
                    !f.canRead() &&
                    (filter == null || filter.accept(f))
            ) {
                indexFile(f);
            }
        }
        // return number of documents indexed
        return writer.numDocs();
    }

    private static class TextFilesFilter implements FileFilter {
        public boolean accept(File path) {
            // Index .txt file only, using FileFilter
            return path.getName().toLowerCase().endsWith(".txt");
        }
    }

    protected Document getDocument(File f) throws Exception {
        Document doc = new Document();
        // Index file content
        doc.add(new Field("contents", new FileReader(f)));
        // Index filename
        doc.add(new Field("filename", f.getName(),
                Field.Store.YES, Field.Index.NOT_ANALYZED));
        // Index file path
        doc.add(new Field("fullpath", f.getCanonicalPath(),
                Field.Store.YES, Field.Index.NOT_ANALYZED));
        return doc;
    }

    private void indexFile(File f) throws Exception {
        System.out.println("Indexing = " + f.getCanonicalPath());
        Document doc = getDocument(f);
        // Add document to Lucene index
        writer.addDocument(doc);
    }
}
