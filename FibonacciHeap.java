
/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap
{
	public int size;
	public int treeCount;
	public int markedCount;
	public static int linksCount;
	public static int cutsCount;
	public HeapNode minNode;
	public HeapNode first;

   /**
    * public boolean isEmpty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    *   
    */
    public boolean isEmpty()
    {
    	return (size() == 0); //size runs in O(1)
    }
    //Total: O(1)
    
		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
    * 
    * Returns the new node created. 
    */
    public HeapNode insert(int key)
    {
    	HeapNode toInsert = new HeapNode(key); //create new node O(1)
    	insertNode(toInsert); //insertNode runs in O(1)
    	return toInsert;
    }
    //Total: Worst Case: O(1)  ;  Amortized: O(1)
    
    public void insertNode(HeapNode toInsert) //This function will recieve a node toInsert, and insert it to this heap
    {
    	if (this.first == null) //if this heap is empty (currently no trees)
    	{
    		this.first = toInsert;
    		toInsert.setNext(toInsert);
    		toInsert.setPrev(toInsert); //List of trees is circular
    		this.setMinNode(toInsert); //Only node currently, so also the minimal key
    	}
    	else
    	{
    		//We identify trees with roots. this.first is the first root in the list
    		
    		HeapNode heapFirst = this.first;
        	HeapNode heapLast = this.first.getPrev(); //Current last root
        	
        	this.first = toInsert; //Setting the first root as toInsert
        	toInsert.setNext(heapFirst); //Pointers adjustments
        	heapFirst.setPrev(toInsert);
        	
        	toInsert.setPrev(heapLast);
        	heapLast.setNext(toInsert);
    	}
    	
    	this.size+=1; //Another node added to the list
    	this.treeCount+=1; //Node is always added as a separate root
    	
    	if (this.findMin().getKey() > toInsert.getKey()) //Possible that toInsert's key is smaller than minimal. 
    	{
    		this.setMinNode(toInsert); //If so, update minNode to be toInsert (O(1))
    	}
    	
    	//all setters and getters run in O(1), and fields are also accessed in O(1)
    }
    //Total: O(1)

   /**
    * public void deleteMin()
    *
    * Delete the node containing the minimum key.
    *
    */
    public void deleteMin()
    {
    	if (this.isEmpty()) {//if the heap is empty, do nothing. isEmpty() runs in O(1)
			return; //O(1)
		}
    	else if(this.size()==1) {//if it is of size 1, then the minimal node is the only node. therefore, we initialize the fields of the heap accordingly. equivalence check runs in O(1)
    		this.size-=1;//O(1)
    		this.treeCount-=1;//O(1)
    		this.markedCount=0;//O(1)
    		this.setMinNode(null);//O(1)
    		this.first=null;//O(1)
    	}
    	else //otherwise, we have 2 or more items in the heap. so, firstly we disconnect the minimal node from its children (if existing) 
    	{
    		HeapNode child = this.findMin().getChild();//creates a pointer to the child field of the minimal node.
    		HeapNode minNodenext = this.findMin().getNext();//creates a pointer to the next field of the minimal node. 
			FibonacciHeap heap = ChildrentoRoots(child);//creates a heap that contains the children of the to be deleted minimal node, in order of lowest rank to largest. ChildrentoRoots(HeapNode) runs in WC O(n) but O(logn) amortized
			//The above updates treeCount accordingly
			
			if (!heap.isEmpty()) {//if there are children of the to-be-deleted node, we insert them between the to be deleted minimal node and its next node.
				HeapNode correctPrev = heap.first.getPrev();//creates a pointer to the last son.
				this.findMin().setNext(heap.first);//sets the minimal node's next field to her first son. 
				heap.first.setPrev(this.findMin());//sets the son's prev field to the minimal node. 
				minNodenext.setPrev(correctPrev);//sets the node after min 's prev field to the last son, effectively disconnecting it from min.
				correctPrev.setNext(minNodenext);//set's the last son's next field as the node after min. 
			}
			
			this.findMin().getPrev().setNext(this.findMin().getNext());//set the minimal node's prev node's next field to be the next node of the minimal node, effectively disconnecting the min. 
			this.findMin().getNext().setPrev(this.findMin().getPrev());//set the minimal node's next node's prev field to be the prev node of the minimal node, effectively disconnecting the min.
			
			if (this.findMin()==this.first) {//adjusts the first field of the heap if wer'e deleting it.
				this.first=this.findMin().getNext(); 
			}
			
			this.setMinNode(null);//reset the minimal node (because we delete it). setMinNode(HeapNode) runs in O(1)
    		this.treeCount-=1;//updates the treeCount field  because wer'e deleting a root node.
    		int newSize = this.size()-1;//creates a pointer to the new size of the heap to be.
    		this.Consolidate();//consolidates the heap into a non lazy binomial heap. Consolidate runs in Worst Case: O(n)  ;  Amortized: O(log(n))
    		this.size=newSize;//updates accordingly. Assignment runs in O(1)
    	}
    	//All setters, getters, and field accesses run in O(1)
    }
    //Total: Worst Case: O(n)  ;  Amortized: O(log(n))
    
    /**
     * public void Consolidate()
     *
     * proceeds to consolidate the heap into a non lazy binomial heap as seen in class
     *
     */
     public void Consolidate()
     {
    	 HeapNode[] arr = new HeapNode[(int) Math.ceil((Math.log(this.size()))/(Math.log((1+Math.sqrt(5.0))/2)))];
    	 //creates an array in which every index represents the rank of the tree put in it. Needed max size O(log(n)) as proved in lecture (tree of rank k has at least phi^k nodes)
    	 
    	 HeapNode curr = this.first;//creates a pointer to the first tree root in the heap.
    	 int tree_index = curr.getRank();//creates a variable that keeps track of the value of the current tree root's rank.
    	 int OGtreecount = this.treeCount;//saves the number of trees currently in the heap
    	 
    	 for (int i = 1; i <= OGtreecount; i++)  //iterates over the trees in the heap. runs for #treeCount iterations. #treeCount is O(n) WC but O(logn) amortized as part of DeleteMin series
    	 {
    		 HeapNode secureNextcurr = curr.getNext();//saves pointer to the next tree root we will need to access
    		 if (arr[tree_index]==null) //if there's no other tree of the same rank in the array, we place the tree in the index appropriate for it's rank. 
    		 {
    			 arr[tree_index]=curr;//puts the tree root in the array index fitting to its rank.
    		 }
    		 else //or else, there is another tree of the same rank. so, we link both of them appropriately to their keys and put them in the next index. vice versa if the next index already contains another tree 
    		 {
    			 HeapNode temp = curr;//creates a pointer to the currently accessed tree with the same rank as the one already existing in the "tree_index" index.
    			 while (arr[tree_index]!=null) //while there continues to exist another tree of the same rank that is already in the output array, we link the two together until we get to a free cell of the correct index fitting for the rank of the newly linked tree. runs #linksforcurrentnode iterations 
    			 {
    				 HeapNode other = arr[tree_index];//creates a pointer to the already existing tree of the same rank in the array.
        			 arr[tree_index]=null;//as we are going to link the trees, we clear the cell.
        			 
        			 if (other.getKey()<temp.getKey()) //if the already existing tree's node key is smaller than the current one, we link the current to the already existing one (to keep the heap invariant). 
        			 {
        				 other.linkAsChild(temp);//link operation. linkAsChild(HeapNode) runs in O(1)
        				 temp = other;//update "temp" to keep track of the newly linked tree for further check of the next index of the array. 
        			 }
        			 else //mirror case of the link operation 
        			 {
        				 temp.linkAsChild(other);//linkAsChild(HeapNode) runs in O(1)
        			 }
        			 tree_index++;//Increases the value of the current linked tree root's rank (because the link operation causes the rank to increase by 1).
				}
    			arr[tree_index]=temp;//puts the tree root in the array index fitting to its rank.
    			
    		 }
    		 
    		 curr=secureNextcurr;//continues to the next tree root in the heap.
    		 tree_index=curr.getRank();//updates accordingly.
		}
    	 
    	//resets the first and treeCount fields of the heap for the insertion of the now consolidated trees.
    	 this.first=null;
    	 this.treeCount=0;
    	 
    	 for (int i = arr.length-1; i >= 0; i--) //iterates over arrays' trees from end to beginning in order to insert (each as leftmost) the now consolidated trees, so ranks are in ascending order in the heap's tree roots list.
    	 { // iterates for O(log(n)) iterations, each in O(1) time
    		 if (arr[i]!=null) 
    		 {
 				this.insertNode(arr[i]);//insertNode runs in O(1), inserts as leftmost
 				
 				if (arr[i].getMark() == true)//if marked, we reset it to be false and decrease the markedCount field by 1 (because roots are never marked). getMark() runs in O(1)
 				{
 					arr[i].setMark(false);//setMark(boolean) runs in O(1)
 					this.markedCount = this.markedCount -1;
 				}
 			}
		}
    	 
    	 //All getters, settesr, and field accesses run in O(1)
     }
     //Total: Worst Case: O(n)  ;  Amortized as part of a DeleteMin series: O(log(n))

     public FibonacciHeap ChildrentoRoots(HeapNode child)
     {
    	 FibonacciHeap heap = new FibonacciHeap();//creates a new fibonacci heap to contain the tree root list of the minimal node's children. FibonacciHeap() runs in O(1)
    	 
    	 if (child!=null) //if min has children, disconnect them from the original node (reset all their fields) and insert them to the newly created heap so that their original list order as children remains. 
    	 {
    		 HeapNode OGprev = child.getPrev();//saves pointer to the minimal node's first son.
    		 HeapNode curr = child.getPrev();//creates a variable that points to the current son of the children's list.
    		 
    		//iterates until we insert all children root nodes into the newly created heap. first reset all its fields connected to the original heap.
    		 //Using a new helper fibHeap here has the same time complexity as any other data structure since needed to reset Parent fields anyway
    		 // WC O(n) Iterations, amortized as part of DeleteMin series O(log(n)) iterations (because we proved fib heap's trees' degrees are at most O(log(n)) right after DeleteMin)
        	 do 
        	 {
        		 HeapNode nextSon = curr.getPrev();//saves pointer to the prev field of the currently accessed son's root node (the next root node to go through).
        		 
        		 //Disconnecting it from the min:
        		 curr.setParent(null);
        		 curr.setNext(null);
        		 curr.setPrev(null);
        		 
        		 if (curr.getMark()==true) 
        		 {
					this.markedCount--;//if the root node was marked, we now set it to false (because it'll be a root in the original heap) so we subtract from the markedCount field.
        		 }
        		 curr.setMark(false);
        		 
        		 heap.insertNode(curr);//inserNode(HeapNode) runs in O(1)
        		 this.treeCount++;
        		 curr=nextSon;//Assigns curr to be nextSon, which will be added to the heap in next iteration
        	 }
        	 while(curr!=OGprev);
    	 }
    	 
    	 return heap;//returns the heap with the children's list as its tree root nodes list.
    	 
    	//All getters, settesr, and field accesses run in O(1)
     }
   //Total: Worst Case: O(n);  Amortized as part of a DeleteMin series: O(log(n))
     
   /**
    * public HeapNode findMin()
    *
    * Return the node of the heap whose key is minimal. 
    *
    */
    public HeapNode findMin()
    {
    	return this.minNode; //field access in constant time
    }
    //Total: Worst Case: O(1)  ;  Amortized: O(1)
    
    public void setMinNode(HeapNode node)
    {
    	this.minNode = node; //field access in constant time
    }
    //Total: O(1)
    
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Meld the heap with heap2
    *
    */
    public void meld (FibonacciHeap heap2)
    {
    	if (heap2.isEmpty() == false)
    	{
    		//Saving pointers
    		HeapNode oneFirst = this.first;
    		HeapNode oneLast = this.first.getPrev(); //last tree of this
         	HeapNode twoFirst = heap2.first;
         	HeapNode twoLast = heap2.first.getPrev(); //last tree of heap2
         	
         	//Concatenate this's list of trees with heap2's list of trees afterwards, by setting the next,prev fields
         	oneLast.setNext(twoFirst);
         	twoFirst.setPrev(oneLast);
         	twoLast.setNext(oneFirst);
         	oneFirst.setPrev(twoLast);
         	
         	this.treeCount = this.treeCount + heap2.treeCount; //treeCount will grow since heap2 has trees
         	this.size = this.size + heap2.size; //size will grow since heap2 has nodes
         	this.markedCount = this.markedCount + heap2.markedCount; //markedCount might grow since heap2 might have marked nodes
         	
         	if (this.findMin().getKey() > heap2.findMin().getKey()) //If heap2 has a node with smaller key than this's minimal key
         	{
         		this.setMinNode(heap2.findMin()); //Update this's minNode which points to the node with minimal key
         	}
         	
         	//All setters, getters, calculations and field accesses above run in O(1)

    	} //If heap2 is empty, nothing is required.
    	 
    	  
    }
    //Total: Worst Case: O(1)  ;  Amortized: O(1)

   /**
    * public int size()
    *
    * Return the number of elements in the heap
    *   
    */
    public int size()
    {
    	return this.size; //field access in constant time. The field contains the number of nodes in the heap
    }
    //Total: O(1)
    	
    /**
    * public int[] countersRep()
    *
    * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
    * 
    */
    public int[] countersRep()
    {
    	if (this.treeCount==0) { //If there are no trees, return empty array of size 0
    		int[] arr = new int[0];
    		return arr;
		}
    	
    	//Iterate over heap's trees, find max rank
        HeapNode node = this.first;
        int maxRank = 0;
        if (node != null)
        {
            do //do-while used because list is circular, iterate from first until before first is reached again
            {
                if (node.getRank() > maxRank)
                {
                    maxRank=node.getRank();
                }    
                node = node.getNext();
            }
            while(node != first);
            //O(#trees). In the worst case, all nodes are roots of trees, so O(n)
        }
        
        int[] arr = new int[maxRank+1]; //an array to count the apperances of all ranks, from 0 to the maxRank found

        //Iterate again over heap's trees, count ranks and update matching array cells accordingly
        node = this.first;
        if (node != null)
        {
            do
            {
                arr[node.getRank()]+=1;
                node = node.getNext();
            }
            while (node != first);
        }
        
        return arr; //     to be replaced by student code
    }
    //Total: O(1+#trees), Worst case O(n)
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap. 
    *
    */
    public void delete(HeapNode x) 
    {    
    	this.decreaseKey(x,Integer.MAX_VALUE - x.getKey() + 1); //Decrease x's key to Integer.MIN_VALUE, now x is the new minNode. Worst Case O(n)
    	this.deleteMin(); //Deletes the minNode, which is x. Worst Case O(n)
    }
    //Total: Worst Case O(n); Amortized O(log n)

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this chage (for example, the cascading cuts procedure should be applied if needed).
    */
    public void decreaseKey(HeapNode x, int delta)
    {    
    	x.setKey(x.getKey() - delta); //decreasing the key
    	
    	if (this.findMin().getKey() > x.getKey()) //x's key changed, update min node if necessary
    	{
    		this.setMinNode(x);
    	}
    	
    	if (x.getParent() != null)
    	{
    		if (x.getKey() < x.getParent().getKey()) //Is there a need for cut?
    		{
    			HeapNode y = x.getParent();
    			cascadingCut(x,y); //Starting the cascading cuts process. O(n) Worst case, O(1) amortized
    		}
    		
    	}
    	
    }
    //Total: O(n) worst case, O(1) amortized
    
    public void cascadingCut(HeapNode x, HeapNode y)
    {
    	cut(x,y); //runs in O(1)
    	if (y.getParent() != null) //y isn't a root so is markable
    	{
    		if (y.getMark() == false) //y wasn't marked
    		{
    			y.setMark(true); //y is marked now, we're done
    			this.markedCount++;
    		}
    		else //y was already marked, cascading cuts: recursive call up in tree
    		{
    			cascadingCut(y,y.getParent()); 
    		}
    	}
    }
    //Total: Worst Case: O(n),  Amortized: O(1)
    
    public void cut(HeapNode x, HeapNode y) //cuts node x from it's parent y
    {
    	if (x.getMark()==true) { //x will be a root soon, so if it was marked, unmark it. 
    		x.setMark(false);
    		this.markedCount--;
		}
    	x.setParent(null); //x will be a root, so x.parent = null
    	
    	y.setRank(y.getRank() - 1); 
    	
    	if (x.getNext() == x) //if x was an only child
    	{
    		y.setChild(null);
    	}
    	else
    	{
    		//disconnect x from it's siblings and connect them to each other
    		HeapNode xNext = x.getNext();
    		HeapNode xPrev = x.getPrev();
    		xPrev.setNext(xNext);
    		xNext.setPrev(xPrev);
    		x.setNext(null);
    		x.setPrev(null);
    		
    		if(y.getChild() == x) //if x was y's leftmost child, set y.child as the node which was x.next before the cut
    		{
    			y.setChild(xNext);
    		}
    		
    		
    	}
    	
    	this.insertNode(x); //insert x as a root, O(1)
    	this.size--; //this.size is increased through insertNode, however the number of nodes in the heap stays the same, so a decreasement is required.
    	cutsCount++;
    	
    	//All getters, setters, and field accesses are in O(1)
    }
    //Total: O(1)
    

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap. 
    */
    public int potential() 
    {    
    	return (this.treeCount + 2 * this.markedCount); //field access in constant time, and so is the calculation
    }
    //Total: O(1)

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the run-time of the program.
    * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
    * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
    * in its root.
    */
    public static int totalLinks()
    {    
    	return linksCount; //field access in constant time
    }
    //Total: O(1)

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the run-time of the program.
    * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
    */
    public static int totalCuts()
    {    
    	return cutsCount; //field access in constant time
    }
    //Total: O(1)

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k minimal elements in a binomial tree H.
    * The function should run in O(k*deg(H)). 
    * You are not allowed to change H.
    */
    public static int[] kMin(FibonacciHeap H, int k)
    {
    	FibonacciHeap fib = new FibonacciHeap(); //A fiboonacci heap will help
        int[] arr = new int[k];
        HeapNode node = H.first;
        for (int i = 0; i<k; i++) //In each iteration, the ith smallest key will be added to the array
        {
        	if (node != null)
        	{
        		HeapNode pos = node;
        		do
        		{
        			HeapNode inserted = fib.insert(pos.getKey());  //inserting an element with the same key to the fib heap
        			inserted.setPointer(pos); //keeping a pointer to the matching element in the binomial tree
        			pos = pos.getNext();
        		}
        		while (pos != node); //traversing all pos's siblings and inserting them to the fib heap, total cost O(k deg(H))
        	}
        	
        	HeapNode min = fib.findMin().getPointer(); //gets the current min from the fib heap's pointer, therefore, gets the ith smallest element from the binomial tree
    		fib.deleteMin(); //deleting the min from the fib heap, total cost O(k deg(H))
    		
    		arr[i] = min.getKey(); //adding the ith samllest key to the array
    		node = min.child; //next iteration, the ith smallest key's node's children will be added to the fib heap
        }
        
        return arr; 
    }
    //Total: O(k deg(H))
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in 
    * another file 
    *  
    */
    public class HeapNode
    {
    	public int key;
    	public int rank;
    	public boolean mark;
    	public HeapNode child;
    	public HeapNode next;
    	public HeapNode prev;
    	public HeapNode parent;
    	public HeapNode pointer;

      	public HeapNode(int key) {
    	    this.key = key;
          }
      	
      	/**
      	 * Precondition: this.key < node.key && this.rank == node.rank
      	 * 
      	 */
      	public void linkAsChild(HeapNode node) //Links node as this's child
      	{
      		//All getters and setters run in O(1)
      		
      		if (this.getChild()==null) { //if this has no children, add node as the first. Circular so node's next,prev point to itself. 
      			this.setChild(node);
          		node.setParent(this);
          		node.setNext(node);
          		node.setPrev(node);
			}
      		else { //this has children, so pointers adjustments are necessary
          		HeapNode firstChild = this.getChild();
          		HeapNode lastChild = this.getChild().getPrev();
          		
          		this.setChild(node);
          		node.setParent(this);
          		
          		node.setNext(firstChild);
          		firstChild.setPrev(node);
          		
          		node.setPrev(lastChild);
          		lastChild.setNext(node);
      		}
      		
      		FibonacciHeap.linksCount+=1; //Accessing field in constant time. A link was made so increase linksCount
      		this.setRank(this.getRank()+1); //this has now a new child
      		
      	}
      	//Total: O(1)
      	

      	public int getKey() { //returns this's key
    	    return this.key; //Accessing field in constant time
          }
      	//Total: O(1)
      	
      	
      	public void setKey(int key) { //sets this's key field
    		this.key = key; //Accessing field in constant time
    	}
      	//Total: O(1)

      	
    	public int getRank() { //returns number of this's children - this's rank
    		return this.rank; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public void setRank(int rank) { //sets that field rank which counts this's children 
    		this.rank = rank; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public boolean getMark() { //returns true if this node is marked, false otherwise
    		return this.mark; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public void setMark(boolean mark) { //Sets this node to be marked with arg true, sets to be unmarked with arg false 
    		this.mark = mark; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public HeapNode getChild() { //returns this's child (leftmost)
    		return this.child; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public void setChild(HeapNode child) { //sets pointer to this's child (leftmost)
    		this.child = child; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public HeapNode getNext() { //returns this's next sibling. If it has none, will return itself (implemented in other functions)
    		return this.next; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public void setNext(HeapNode next) { //sets pointer to this's next sibling
    		this.next = next; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public HeapNode getPrev() { //returns this's previous sibling. If it has none, will return itself (implemented in other functions)
    		return this.prev; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public void setPrev(HeapNode prev) { //sets pointer to this's previous sibling
    		this.prev = prev; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public HeapNode getParent() { //returns this's parent. If it has none, will return null
    		return this.parent; //Accessing field in constant time
    	}
    	//Total: O(1)

    	
    	public void setParent(HeapNode parent) { //sets the pointer to this's parent
    		this.parent = parent; //Accessing field in constant time
    	}
    	//Total: O(1)
    	
    	
    	/**Used only for kMin **/
    	public HeapNode getPointer() {
    		return this.pointer; //Accessing field in constant time
    	}
    	//Total: O(1)
    	
    	
    	public void setPointer(HeapNode pointer) {
    		this.pointer = pointer;//Accessing field in constant time 
    	}
    	//Total: O(1)
    	

    }
    
}
