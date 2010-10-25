// array of arrays of read-only documents
@Readonly Document [][] docs1 = 
    new @Readonly Document [2][12]; 

// read-only array of arrays of documents
Document @Readonly [][] docs2 = 
    new Document @Readonly [2][12]; 

// array of read-only arrays of documents
Document [] @Readonly [] docs3 = 
    new Document[2] @Readonly [12];
