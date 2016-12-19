import org.jacop.core.*;import java.io.BufferedReader;import java.io.File;import java.io.FileReader;import java.io.IOException;import java.io.PrintWriter;import java.util.ArrayList;import org.jacop.search.*;import org.jacop.jasat.utils.structures.* ;import org.jacop.satwrapper.*;public class SATPacman {		//ATTRIBUTES		private char[][] inputMaze = null;   //Char matrix containing the characters of the input file		private int height = 0;			//height of the matrix == number of rows		private int width  = 0;				//width of the matrix == number of columns						private int n_ghosts = 0 ;		public BooleanVar [][][] matrices_bool = null; //Contains the boolean variables		private int [][][]  matrices_literals = null; //Contains the literals used to create the actual clauses and then use them to define the SAT problem		private BooleanVar[] allVariables = null ; // variables again but in 1D array --> needed for DFS to work										/*----------------------------------CONSTRUCTOR-----------------------------------------------*/		public SATPacman(String filename, int n_ghosts, Store store, SatWrapper wrapper ) throws IOException{ 			readFileAndSetDimensions(filename); //reads file once to extract dimensions			fillMaze(filename); // reads file again and fills the maze			this.n_ghosts = n_ghosts ; //Assign number of ghosts			matrices_bool = new BooleanVar[n_ghosts+1][this.height][this.width] ;  			matrices_literals =  new int [n_ghosts+1][this.height][this.width] ; 			initializeBoolVariables(store, wrapper);  //Initializes all boolVariables and fills the matrix of literals			allVariables = new BooleanVar[height*width*(n_ghosts+1)] ;  //All variables in an array as we need an array for solving			setArray() ;								}			/*--------------------------------------------------------------------------------------------*/				/*------------------------------------AUX FUNCTIONS------------------------------------------*/		void readFileAndSetDimensions(String filename) throws IOException {//Fills the maze of chars used to extract information on empty cells			/**			 * Auxiliary Function: readFileAndSetDimensions()			 * 			Arguments : 			 * 				String filename = path of a file to be read			 * 		Reads the whole file and extracts the dimensions of the Maze			 */			BufferedReader read = new BufferedReader(new FileReader(filename));						String readline;			int rows = 0; //will			char[] ch = null ;						while((readline = read.readLine()) != null){				ch = readline.toCharArray();   //read a line and store it in a character array								rows++;			}						this.width=ch.length;			this.height=rows;						read.close();													}		void fillMaze (String filename) throws IOException{			/**			 * Auxiliary Function: fillMaze()			 * 		String filename = path to a file			 * 	Attributes initialization purposes			 * 	Requires that readFileAndSetDimensions() in order to work properly			 */			BufferedReader read2 = new BufferedReader(new FileReader(filename));			String readline2;			int num = 0;			inputMaze = new char[height][width];			while((readline2 = read2.readLine()) != null){				char[] ch2 = readline2.toCharArray();					inputMaze[num] = ch2;				num++;			}			read2.close();					}		void initializeBoolVariables( Store store, SatWrapper wrapper)  {				/**				 * Initializes the Variables and literals matrixes				 * Needs readFileAndSetDimensions() and fillMaze() to have been called				 */						int n_rows = this.height ;			int m_columns = this.width;						/* PACMAN VARS INITIALIZATION */			for(int x=0 ; x<n_rows ; x++ ){				for(int y=0 ; y<m_columns ; y++ ){					matrices_bool[0][x][y] = new BooleanVar(store, "p("+x+","+y+")" ); // variables which represent whether the pacman is 					//in a given cell or not 0->nope  1->Yes					wrapper.register(matrices_bool[0][x][y]);										matrices_literals[0][x][y] = wrapper.cpVarToBoolVar(matrices_bool[0][x][y],1,true);									}			}										/* GHOST VARS INITIALIZATION */			for (int g=1 ;g<(n_ghosts+1); g++ ){				for(int x=0 ; x<n_rows ; x++ ){					for(int y=0 ; y<m_columns ; y++ ){						matrices_bool[g][x][y] = new BooleanVar(store, "g"+g+"("+x+","+y+")" ); // variables which represent whether the a ghost (g) is 					//in a given cell or not 0->nope  1->Yes						wrapper.register(matrices_bool[g][x][y]);												matrices_literals[g][x][y] = wrapper.cpVarToBoolVar(matrices_bool[g][x][y],1,true);					}				}			}				}		void setArray() {								ArrayList<BooleanVar>  aux = new ArrayList<BooleanVar>() ;						for(int g=0; g<this.n_ghosts+1 ; g++){				for(int i=0; i<this.height ; i++){					for(int j=0; j<this.width ; j++){						aux.add(matrices_bool[g][i][j]) ;					}				}			}						aux.toArray(this.allVariables);		}		/*--------------------------------------------------------------------------------------------*/				/*------------------------------------CONSTRAINTS------------------------------------------*/		void only_one_Pacman ( Store store, SatWrapper wrapper){						for(int x=0 ; x<this.height ; x++ ){				for(int y=0 ; y<this.width ; y++ ){					for(int k=0 ; k<this.height ; k++ ){						for(int j=0 ; j<this.width  ; j++ ){							IntVec clause = new IntVec(wrapper.pool); //Clause set creation							if(x==k && y==j){ // Avoid -p[x][y] v -p[x][y]								//DO NOTHING!!!!								//System.out.println("Avoided comparison with itself");							}							else{  //Compare with all the other cells								clause.add(- matrices_literals[0][x][y]) ;  								clause.add(- matrices_literals[0][k][j]);								wrapper.addModelClause(clause.toArray()) ;// -p[x][y] v -p[k][j]							}													}// for j					}//for k				}//for y			}//for x					}		void placeInEmptyCellsOnly( Store store, SatWrapper wrapper){						//System.out.println();			//System.out.println("-----Empty Cells Constraints-----");						for(int entity=0 ; entity<this.n_ghosts+1 ; entity++ ){				for(int x=0 ; x<this.height ; x++ ){					for(int y=0 ; y<this.width ; y++ ){						if(inputMaze[x][y]=='%' || inputMaze[x][y]=='O'){   //Pacman nor Ghosts cannot be placed on a nonempty cell							//System.out.println("No se pondr� un pacman en"+x+y);							IntVec clause = new IntVec(wrapper.pool); //Clause  creation								clause.add(- matrices_literals[entity][x][y]); //single negated literal clause																		if(entity==0){										//System.out.println("-p"+entity+"("+x+","+y+")"  );									}									else if (entity!=0){										//System.out.println("-g"+entity+"("+x+","+y+")"  );									}																wrapper.addModelClause(clause.toArray()) ;   //Adding						}					}				}			}						//System.out.println("-----------------------------");								}		void atLeastOnePacman(Store store, SatWrapper wrapper){			IntVec clause = new IntVec(wrapper.pool); //Clause set creation						for(int x=0 ; x<this.height ; x++ ){				for(int y=0 ; y<this.width ; y++ ){													clause.add( matrices_literals[0][x][y]) ;  						}					}				wrapper.addModelClause(clause.toArray()) ;		}		//WORKS FOR SURE  				void allGhostsPlaced (Store store, SatWrapper wrapper){						 //			for(int g=1 ; g<this.n_ghosts+1 ;g++ ){				IntVec clause = new IntVec(wrapper.pool);				for(int x=0 ; x<this.height ; x++ ){					for(int y=0 ; y<this.width ; y++ ){						clause.add( matrices_literals[g][x][y]) ;  					}				}				wrapper.addModelClause(clause.toArray()) ;			}					}		void noGhostsAroundPacman ( Store store, SatWrapper wrapper){			int rows = this.height ;			int cols = this.width;						for(int g=1 ; g<this.n_ghosts+1; g++){				for(int x=0 ; x<rows ; x++ ){					for(int y=0 ; y<cols ; y++ ){						//						int minuscoordinatex= x-1 ;//						int minuscoordinatey= y-1 ;//						int pluscoordinatex= x+1 ;//						int pluscoordinatey= y+1 ;//																		addClause(wrapper, (- matrices_literals[0][x][y]) ,(- matrices_literals[g][x][y]) );						try{							addClause(wrapper, (- matrices_literals[0][x][y]) ,(- matrices_literals[g][x+1][y]) );						}						catch(Exception e){							//System.out.println("Position("+pluscoordinatex+","+y+") does not exist") ;						}						//------------------------------						try{							addClause(wrapper, (- matrices_literals[0][x][y]) ,( - matrices_literals[g][x-1][y] ) );						}						catch(Exception e){							//System.out.println("Position("+minuscoordinatex+","+y+") does not exist") ;						}												//---------------------------						try{							addClause(wrapper, (- matrices_literals[0][x][y]) ,( - matrices_literals[g][x][y+1]) );						}						catch(Exception e){							//System.out.println("Position("+x+","+pluscoordinatey+") does not exist") ;						}								//---------------------------------												try{							addClause(wrapper, (- matrices_literals[0][x][y]) ,( -	matrices_literals[g][x][y-1] ) );						}						catch(Exception e){							//System.out.println("Position("+x+","+minuscoordinatey+") does not exist") ;						}						//-----------------------------												try{							addClause(wrapper, (- matrices_literals[0][x][y]) ,( -	matrices_literals[g][x+1][y+1] ) );													}						catch(Exception e){							//System.out.println("Position("+pluscoordinatex+","+pluscoordinatey+") does not exist") ;						}						//-----------------------------												try{							addClause(wrapper, (- matrices_literals[0][x][y]) ,( - matrices_literals[g][x+1][y-1] ) );													}						catch(Exception e){							//System.out.println("Position("+pluscoordinatex+","+minuscoordinatey+") does not exist") ;						}												try{							addClause(wrapper, (- matrices_literals[0][x][y]) ,(  - matrices_literals[g][x-1][y+1]) );													}						catch(Exception e){							//System.out.println("Position("+minuscoordinatex+","+pluscoordinatey+") does not exist") ;						}						try{							addClause(wrapper, (- matrices_literals[0][x][y]) ,(  -	matrices_literals[g][x-1][y-1] ) );													}						catch(Exception e){							//System.out.println("Position("+minuscoordinatex+","+minuscoordinatey+") does not exist") ;						}					}				}			}		}	 		//WORKS				void eachGhostAssignedToOnePosOnly ( Store store, SatWrapper wrapper){  			/**			 * Given that we have not established constraints to avoid that the same ghost is assigned to 2 positions			 * 		this function is in charge of that. It always avoids a ghost to be placed in a nonempty cell.			 */									for(int g=1; g<this.n_ghosts+1 ; g++){				for(int x=0 ; x<this.height ; x++ ){					for(int y=0 ; y<this.width ; y++ ){						for(int k=0 ; k<this.height ; k++ ){							for(int j=0 ; j<this.width  ; j++ ){								if(x==k && y==j){ //If we are comparing with the same cell									//DO NOTHING!!!!																	}								else{  //Compare with all the other cells									IntVec clause = new IntVec(wrapper.pool); //Clause set creation									clause.add(- matrices_literals[g][x][y]) ;  									clause.add(- matrices_literals[g][k][j]);									wrapper.addModelClause(clause.toArray()) ;// -g[number][x][y] v -g[number][k][j]									//System.out.println("-g"+g+"("+x+","+y+") v "+"-g"+g+"("+k+","+j+")" );								}							}						}					}				}			}		}			 				void noTwoGhostsSameRow( Store store, SatWrapper wrapper){			for(int g1=1; g1<this.n_ghosts+1 ; g1++){				for(int g2=1; g2<this.n_ghosts+1 ; g2++){					for(int x=0 ; x<this.height ; x++ ){						for(int y=0 ; y<this.width ; y++ ){							for(int k=0 ; k<this.width ; k++ ){								if(g1==g2){									//System.out.println("Executed");								}								else{									addClause(wrapper, (- matrices_literals[g1][x][y]) , (- matrices_literals[g2][x][k]));								}							}						}					}				}			}		}														/*--------------------------------------------------------------------------------------------*/										public void PrintMaze (){//Simply prints the input maze; Debugging purposes						System.out.println("Input file:");			for (int row = 0; row < this.height; row++) {				for (int col = 0; col < this.width; col++) {					System.out.print(inputMaze[row][col]);				}				System.out.println();			}						//System.out.println("Height, number of rows:"+this.height);			//System.out.println("Width, number of columns:"+this.width);			System.out.println("Number of ghosts:"+this.n_ghosts);			System.out.println();					}				public static void writeToFileWithCorrectFormat(String completepath, char [][] outMaze){//			//			File fullPath = new File(completepath);//			String directory = fullPath.getParent() ; //Extract the name of the directory//					System.out.println(directory);//			//			String dirName = directory ;//			String newFileName = "hola.txt";//////			File dir = new File (dirName);			File actualFile = new File (completepath + ".output") ;			try{				PrintWriter writer = new PrintWriter(actualFile);				for (int row = 0; row < outMaze.length; row++) {					for (int col = 0; col < outMaze[0].length; col++) {						writer.print(outMaze[row][col]);					}					writer.println();				}				writer.close();			} catch (IOException e) {				// do something			}		}				public static void addClause(SatWrapper satWrapper, int literal1, int literal2){			IntVec clause = new IntVec(satWrapper.pool);			clause.add(literal1);			clause.add(literal2);			satWrapper.addModelClause(clause.toArray());		}				public static void printOutMaze( char[][] outMaze){			System.out.println("Output file:");			for (int row = 0; row < outMaze.length; row++) {				for (int col = 0; col < outMaze[0].length; col++) {					System.out.print(outMaze[row][col]);				}				System.out.println();			}		}					public static void main(String[] args) throws IOException {	//	System.out.println("arg[0]"+args[0]);	//	System.out.println("arg[1]"+args[1]);		if( args.length!=2  ){ //IF CORRECT NUMBER OF ARGUMENTS			System.out.println("You must provide a path and a number of ghosts! , the format of the input is" ) ;			System.out.println( " java SATPacman <pathOfEmptyMaze> <n_ghosts>");			System.out.println("Remember that <n_ghosts> >= 0 and make sure<pathOfEmptyMaze> matches with a file");		}		else{				String inputFile = args[0] ;				int numberOfGhosts ;				try{					numberOfGhosts = Integer.parseInt(args[1]);				}				catch(Exception e)				{					System.out.println("The number of ghosts specified must be a positive number");					return ;				}								if(numberOfGhosts<0){					return;				}											Store store = new Store();			SatWrapper wrapper = new SatWrapper(); 			store.impose(wrapper);							SATPacman AutomatedFilling = new SATPacman(inputFile,numberOfGhosts, store , wrapper);			//SATPacman AutomatedFilling = new SATPacman("C:\\Users\\Nolliejandro\\Dropbox\\testMaze0",2, store , wrapper);			AutomatedFilling.PrintMaze();			/*Imposing constraints */			AutomatedFilling.only_one_Pacman(store, wrapper);			AutomatedFilling.atLeastOnePacman(store, wrapper);			AutomatedFilling.placeInEmptyCellsOnly(store, wrapper);			AutomatedFilling.allGhostsPlaced (store, wrapper);			AutomatedFilling.eachGhostAssignedToOnePosOnly(store, wrapper);			AutomatedFilling.noTwoGhostsSameRow(store, wrapper);			AutomatedFilling.noGhostsAroundPacman(store, wrapper);			/*Solve */			Search<BooleanVar> search = new DepthFirstSearch<BooleanVar>();			SelectChoicePoint<BooleanVar> select = new SimpleSelect<BooleanVar>(AutomatedFilling.getAllVariables() , new SmallestDomain<BooleanVar>(), new IndomainMin<BooleanVar>());			Boolean result = search.labeling(store, select);			//	System.out.println((AutomatedFilling.matrices_bool[0][0][2]).value());			char[][] outputMaze = AutomatedFilling.getInputMaze();   //Char matrix containing the characters to be written in file			if(result){				System.out.println("Solution found");				for(int g=0; g<AutomatedFilling.getN_ghosts()+1 ; g++){					for(int i=0; i< AutomatedFilling.getHeight(); i++){						for(int j=0; j< AutomatedFilling.getWidth(); j++){							if(g==0){ // If a Pacman in position i,j								switch (AutomatedFilling.matrices_bool[g][i][j].value() ){								case 0:									break;								case 1: outputMaze[i][j] = 'P' ;								break;								}							}								else if(g>0){ //If a ghost in i,j								switch (AutomatedFilling.matrices_bool[g][i][j].value() ){								case 0: 									break;								case 1: outputMaze[i][j] = 'G' ;								break;								}							}							else{								System.out.println("Impossible case");							}						}					}				}				printOutMaze(outputMaze);				writeToFileWithCorrectFormat(inputFile, outputMaze);			}			else{				System.out.println("No solution was found");			}		}	}		public BooleanVar[] getAllVariables() {		return allVariables;	}	public void setAllVariables(BooleanVar[] allVariables) {		this.allVariables = allVariables;	}	public char[][] getInputMaze() {		return inputMaze;	}	public void setInputMaze(char[][] inputMaze) {		this.inputMaze = inputMaze;	}	public int getHeight() {		return height;	}	public void setHeight(int height) {		this.height = height;	}	public int getWidth() {		return width;	}	public void setWidth(int width) {		this.width = width;	}	public int getN_ghosts() {		return n_ghosts;	}	public void setN_ghosts(int n_ghosts) {		this.n_ghosts = n_ghosts;	}	public BooleanVar[][][] getMatrices_bool() {		return matrices_bool;	}	public void setMatrices_bool(BooleanVar[][][] matrices_bool) {		this.matrices_bool = matrices_bool;	}	public int[][][] getMatrices_literals() {		return matrices_literals;	}	public void setMatrices_literals(int[][][] matrices_literals) {		this.matrices_literals = matrices_literals;	}}	