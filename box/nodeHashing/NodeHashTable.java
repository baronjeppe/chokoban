package box.nodeHashing;

import box.Node;

public class NodeHashTable
{
    private static final int DEFAULT_TABLE_SIZE = 11;

    private HashEntry [ ] array; // The array of elements
    private int currentSize;              // The number of occupied cells
    private int collisions = 0;
    
    /**
     * Construct the hash table.
     */
    public NodeHashTable( )
    {
        this( DEFAULT_TABLE_SIZE );
    }

    /**
     * Construct the hash table.
     * @param size the approximate initial size.
     */
    public NodeHashTable( int size )
    {
    	int temp = size;
    	if (!isPrime(temp))
    		temp = nextPrime(temp);
    	
		allocateArray( temp );
        makeEmpty( );
    }
    public int getSize( )
    {
    	return currentSize;
    }
    public int getArraySize( )
    {
    	return array.length;
    }
    /**
     * Make the hash table logically empty.
     */
    public void makeEmpty( )
    {
        currentSize = 0;
        for( int i = 0; i < array.length; i++ )
            array[ i ] = null;
    }

    /**
     * Internal method to allocate array.
     * @param arraySize the size of the array.
     */
    private void allocateArray( int arraySize )
    {
        array = new HashEntry[ arraySize ];
    }


        /**
     * Find an item in the hash table.
     * @param x the item to search for.
     * @return the matching item.
     */
    public boolean contains( Node x )
    {
        int currentPos = findPos( x );
        return isActive( currentPos );
    }

    /**
     * Method that performs quadratic probing resolution.
     * @param x the item to search for.
     * @return the position where the search terminates.
     */
    
    public int findPos( Node x )
    {
        int offset = 1;
        int startPos = hash( x, array.length );
        int currentPos = startPos;
        
        while( array[ currentPos ] != null && !array[ currentPos ].element.equals( x ) )
        {
        	collisions++;
            currentPos = startPos + (offset * offset);  // Compute ith probe using quadratic probing
            offset += 1;
            if( currentPos >= array.length )
                currentPos -= array.length;
        }
        return currentPos;
    }
    
    public int getCollisions()
    {
    	return collisions;
    }
    /**
     * Return true if currentPos exists and is active.
     * @param currentPos the result of a call to findPos.
     * @return true if currentPos is active.
     */
    private boolean isActive( int currentPos )
    {
        return array[ currentPos ] != null && array[ currentPos ].isActive;
    }

    /**
     * Insert into the hash table. If the item is
     * already present, do nothing.
     * @param x the item to insert.
     */
    public void insert( Node x )
    {
            // Insert x as active
        int currentPos = findPos( x );
        if( isActive( currentPos ) )
            return;

        array[ currentPos ] = new HashEntry( x, true );

            // Rehash; see Section 5.5
        if( ++currentSize > array.length / 2 )
        	rehash( );
    }

    /**
     * Remove from the hash table.
     * @param x the item to remove.
     */
    public void remove( Node x )
    {
        int currentPos = findPos( x );
        if( isActive( currentPos ) )
        {
            array[ currentPos ].isActive = false;
        }
    }


    public static class HashEntry
    {
        public Node  element;  // the element
        public boolean isActive;  // false if marked deleted

        public HashEntry( Node e )
          { this( e, true ); }

        public HashEntry( Node e, boolean i )
          { element  = e; isActive = i; }
          
        public String toString()
        {
            return element.toString();
        }
    }

   
    /**
     * Rehashing for quadratic probing hash table.
     */
    private void rehash( )
    {
        HashEntry [ ] oldArray = array;

            // Create a new double-sized, empty table
        allocateArray( nextPrime( 2 * oldArray.length ) );
        currentSize = 0;

            // Copy table over
        for( int i = 0; i < oldArray.length; i++ )
            if( oldArray[ i ] != null && oldArray[ i ].isActive )
                insert( oldArray[ i ].element );
    }
       
    public static int hash( Node key, int tableSize )
    {
        int hashVal = 0;

        for( int i = 0; i < key.state.map.length; i++ )
        	for (int j = 0; j < key.state.map[0].length; j++)
        		hashVal = 37 * hashVal + key.state.map[i][j] + 37;

        hashVal %= tableSize;
        if( hashVal < 0 )
            hashVal += tableSize; 
        return hashVal;
    }
    public static int simpleHash( String key, int tableSize )
    {
        int hashVal = 0;

        for( int i = 0; i < key.length( ); i++ )
            hashVal += key.charAt( i );

        hashVal %= tableSize;
        if( hashVal < 0 )
            hashVal += tableSize; 
        return hashVal;
    }
      
    public HashEntry getPos(int pos)
    {
        return array[pos];
    }
    
    public int getNoOfElements()
    {
    	int noOfElements = 0;
    	for (int i = 0; i<array.length; i++)
    		if (isActive(i))
    			noOfElements++;
    	return noOfElements;
    }

    public float getLoadFactor()
    {
    	float noOfElements = 0;
    	for (int i = 0; i<array.length; i++)
    		if (isActive(i))
    			noOfElements++;
    	
    	return noOfElements/(float)array.length;
    }

    private static int nextPrime( int n )
        {
            if( n % 2 == 0 )
                n++;

            for( ; !isPrime( n ); n += 2 )
                ;

            return n;
        }

        /**
         * Internal method to test if a number is prime.
         * Not an efficient algorithm.
         * @param n the number to test.
         * @return the result of the test.
         */
        private static boolean isPrime( int n )
        {
            if( n == 2 || n == 3 )
                return true;

            if( n == 1 || n % 2 == 0 )
                return false;

            for( int i = 3; i * i <= n; i += 2 )
                if( n % i == 0 )
                    return false;

            return true;
        }


}