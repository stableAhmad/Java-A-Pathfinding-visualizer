/*
edge cases
-start cell and edge cell can be the same cell (not handled)
-start or end cell can be chosen as barrier cell (not handled)
*/
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComponent;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Graphics2D;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent ;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class pathfinding_main
{
	static int cell_side = 20;//used inside the mouse listener class 
	static int window_width = 800 ;
	static int cells_number = 40;
	static int window_height = 800;
	static int bar_height = 30; // used inside the mouse listener class

	
    static Node grid [][] = new Node[cells_number-2][cells_number];
	static Rectangle startRect , endRect;
	static Color red ;
	
	static Color endColor = new Color(0,102,0);
	static Node startRectangle , goalRectangle;
	static boolean setStartOnClick = false, setGoalOnClick = false , setBarrierOnClick = false;
	static Color startColor = new Color(165,57,30);
	static HashSet<Rectangle> barriers = new HashSet<>();
	
	
	public static void init_grid(Node grid [][])
	{
		for(int i = 0; i <grid.length ; i++)
		{
			for(int j = 0 ; j<grid[0].length ; j++)
			{
				Rectangle rect = new Rectangle(0+(j*cell_side),0+(i*cell_side)+bar_height,cell_side ,cell_side);
				int fs = Integer.MAX_VALUE;
				int ss = Integer.MAX_VALUE;
				int ts = Integer.MAX_VALUE;
				Node tempNode=  new Node(fs,ss,ts , rect);
				grid[i][j] = tempNode;
			}
		}
	}
	
	
	
	
	private static int secondScore(int x1, int y1 , int x2 , int y2)
	{
		int s = Math.abs(x1-x2) + Math.abs(y1-y2);
		return s;
	}
	
	public static void main(String []args)
	{
		//creating a window
		window_width += 17 ;
		window_height += 15*2 ;

	
		//initing the grid
		init_grid(grid);

		
		Window WIN = new Window("Pathfinding Algorithm");
		
		WIN.setSize(window_width,window_height);
		WIN.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WIN.setResizable(false);
		
		winContainer container = new winContainer(grid , window_width , window_height);
		
		JButton setStart = new JButton("set start cell");
		setStart.setBounds(0,5,120,20);
		setStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(startRectangle == null && (!setStartOnClick) )
				{
					setStartOnClick =  true;
					setGoalOnClick = false;
					setBarrierOnClick = false;
				}
			}
		});
		
		JButton setGoalCell = new JButton("set goal cell");
		setGoalCell.setBounds(125,5,120,20);
		setGoalCell.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(goalRectangle == null && (!setGoalOnClick) )
				{
					setGoalOnClick =  true;
					setStartOnClick = false;
					setBarrierOnClick = false;
				}
			}
		});
		

		JButton makeBarrier = new JButton("make barrier");
		makeBarrier.setBounds(250,5,120,20);
		makeBarrier.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(  (!setBarrierOnClick))
				{
					setBarrierOnClick =  true;
					setGoalOnClick = false;
					setStartOnClick = false;
				}
			}
		});
		
		JButton clear = new JButton("clear");
		clear.setBounds(500 , 5 , 120 ,20);
		clear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				container.repaint();
				setBarrierOnClick = setGoalOnClick = setStartOnClick = false;
				startRectangle = goalRectangle = null;
				barriers.clear();
				for(Node [] r : grid)
				{
					for(Node n : r)
					{
						n.path.clear();
						n.firstScore = n.secondScore = n.totalScore = Integer.MAX_VALUE;
					}
				}
			}
		});
		
		
		
		JButton start = new JButton("start");
		start.setBounds(375 , 5 , 120 ,20);
		start.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				
				algoStart((Graphics2D)container.getGraphics() , startRectangle , goalRectangle ,  barriers);
			}
		});
		
		
		
		WIN.setContentPane(container);
		
		container.add(setStart);
		container.add(setGoalCell);
		container.add(makeBarrier);
		container.add(start);
		container.add(clear);
		
		WIN.setVisible(true);
		
		
		MouseAdapter adapter = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				int col = (e.getX()-7)/20;
				int row  = (e.getY()-60)/20;
				if(setStartOnClick)
				{
					
						Graphics2D g2d = (Graphics2D)container.getGraphics();
						g2d.setColor(startColor);
						g2d.fill(grid[row][col].rect);
						startRectangle = grid[row][col];
						setStartOnClick = false;
					
				}else if(setGoalOnClick )
				{
					Graphics2D g2d = (Graphics2D)container.getGraphics();
					g2d.setColor(endColor);
					g2d.fill(grid[row][col].rect);
					goalRectangle = grid[row][col];
					setGoalOnClick = false;
				}else if(setBarrierOnClick)
				{
					Graphics2D g2d = (Graphics2D)container.getGraphics();
					g2d.setColor(Color.BLACK);
					barriers.add(grid[row][col].rect);
					g2d.fill(grid[row][col].rect);
				}
			}

		};
		MouseMotionAdapter mma = new MouseMotionAdapter()
		{
			
			public void mouseDragged(MouseEvent e)
			{
				
				if(setBarrierOnClick)
				{
					Graphics2D g2d = (Graphics2D)container.getGraphics();
					int col = (e.getX()-7)/20;
					int row  = (e.getY()-60)/20;
					g2d.setColor(Color.BLACK);
					barriers.add(grid[row][col].rect);
					g2d.fill(grid[row][col].rect);
					
				}
			}
			
		};
		
		WIN.addMouseListener(adapter);
		WIN.addMouseMotionListener(mma);
	
		
		
	}
		private static void algoStart(Graphics2D g2d , Node s , Node e , HashSet<Rectangle> barriers)
		{
			PriorityQueue<Node> PQ = new PriorityQueue<>();  // poll add peek remove iterator = pq.iterator and hasNext
			HashSet<Node> considered  = new HashSet<>();
			
			s.firstScore = 0;
			s.secondScore = 0;
			s.totalScore = 0;
			
			//problem in the path
			
			PQ.add(s);
			boolean found = false;
			int iteration = 0 ;
			
			while( ! PQ.isEmpty())
			{
				if(iteration == 500 ) break;
					
					Node current = PQ.poll();
					if(!current.equals(s))
					{
						g2d.setColor(new Color(228 , 0 , 0));
						g2d.fill(current.rect);
					}
					considered.add(current);
					int currentCol = (int)(current.rect.getX())/20;
					int currentRow  = (int)(current.rect.getY()-30)/20;
					
					
					System.out.println(currentRow+" "+currentCol);
					
					int newFirstScore = current.firstScore+1;

					
					if(validNeighbor(currentRow , currentCol+1)) // if right exists
					{
						
						
						if(!barriers.contains(grid[currentRow][currentCol+1].rect)) // if its not a barrier
						{
							
							
								
								if(newFirstScore < grid[currentRow][currentCol+1].firstScore )
								{
									
									if(PQ.contains( grid[currentRow][currentCol+1]))
								{
									PQ.remove(grid[currentRow][currentCol+1]);
								}
									grid[currentRow][currentCol+1].path.clear();
									for(int i = 0 ; i <current.path.size() ; i++)
								{
									grid[currentRow][currentCol+1].path.add(current.path.get(i));
								}
								grid[currentRow][currentCol+1].path.add(current.rect);
								
								
								grid[currentRow][currentCol+1].firstScore = current.firstScore+1;
								grid[currentRow][currentCol+1].secondScore = secondScore((int)(grid[currentRow][currentCol+1].rect.getX()) , (int)(grid[currentRow][currentCol+1].rect.getY()) ,(int) (e.rect.getX() ),(int)( e.rect.getY()));
								grid[currentRow][currentCol+1].totalScore = grid[currentRow][currentCol+1].firstScore + grid[currentRow][currentCol+1].secondScore;
								
								if(grid[currentRow][currentCol+1].equals(e))
									{
										found = true;
										finishAlg(grid[currentRow][currentCol+1].path , g2d );
										break;
									}else
									{
										g2d.setColor(new Color(235,117,0));
										g2d.fill(grid[currentRow][currentCol+1].rect);
										PQ.add(grid[currentRow][currentCol+1]);
									}
								} 
								
								
							
						}
					}
					
					if(validNeighbor(currentRow , currentCol-1)) // if left exists
					{
						System.out.println("second condition");
						if(!barriers.contains(grid[currentRow][currentCol-1].rect)) // if its not a barrier
						{
							
							if(newFirstScore < grid[currentRow][currentCol-1].firstScore )//!considered.contains(grid[currentRow][currentCol-1]))
							{
								
								if(PQ.contains( grid[currentRow][currentCol-1]))
								{
									PQ.remove(grid[currentRow][currentCol-1]);
								}
								
								
								grid[currentRow][currentCol-1].path.clear();
								for(int i = 0 ; i <current.path.size() ; i++)
								{
									grid[currentRow][currentCol-1].path.add(current.path.get(i));
								}
								grid[currentRow][currentCol-1].path.add(current.rect);
								
								grid[currentRow][currentCol-1].firstScore = current.firstScore+1;
								grid[currentRow][currentCol-1].secondScore = secondScore((int)(grid[currentRow][currentCol-1].rect.getX()) , (int)(grid[currentRow][currentCol-1].rect.getY()) ,(int) (e.rect.getX() ),(int)( e.rect.getY()));
								grid[currentRow][currentCol-1].totalScore = grid[currentRow][currentCol-1].firstScore + grid[currentRow][currentCol-1].secondScore;
								
								if(grid[currentRow][currentCol-1].equals(e))
									{
										found = true;
										finishAlg(grid[currentRow][currentCol-1].path , g2d );
										break;
									}else
									{
										g2d.setColor(new Color(235,117,0));
										g2d.fill(grid[currentRow][currentCol-1].rect);
										PQ.add(grid[currentRow][currentCol-1]);
									}
							} 
						}
					}
					if(validNeighbor(currentRow+1 , currentCol)) // if down exists
					{
						System.out.println("third condition");
						
						if(!barriers.contains(grid[currentRow+1][currentCol].rect)) // if its not a barrier
						{
							
							if(newFirstScore < grid[currentRow+1][currentCol].firstScore )//!considered.contains(grid[currentRow+1][currentCol]))
							{
								if(PQ.contains( grid[currentRow+1][currentCol]))
								{
									PQ.remove(grid[currentRow+1][currentCol]);
								}
								
								grid[currentRow+1][currentCol].path.clear();
								for(int i = 0 ; i <current.path.size() ; i++)
								{
									grid[currentRow+1][currentCol].path.add(current.path.get(i));
								}
								grid[currentRow+1][currentCol].path.add(current.rect);
								
								grid[currentRow+1][currentCol].firstScore = current.firstScore+1;
								grid[currentRow+1][currentCol].secondScore = secondScore((int)(grid[currentRow+1][currentCol].rect.getX()) , (int)(grid[currentRow+1][currentCol].rect.getY()) ,(int) (e.rect.getX() ),(int)( e.rect.getY()));
								grid[currentRow+1][currentCol].totalScore = grid[currentRow+1][currentCol].firstScore + grid[currentRow+1][currentCol].secondScore;
								
								if(grid[currentRow+1][currentCol].equals(e))
									{
										found = true;
										finishAlg(grid[currentRow+1][currentCol].path , g2d );
										break;
									}else
									{
										g2d.setColor(new Color(235,117,0));
										g2d.fill(grid[currentRow+1][currentCol].rect);
										PQ.add(grid[currentRow+1][currentCol]);
									}
							} 
								
						}
					}
					if(validNeighbor(currentRow-1 , currentCol)) // if up exists
					{
						System.out.println("forth condition");
						if(!barriers.contains(grid[currentRow-1][currentCol].rect)) // if its not a barrier
						{
							
							if(newFirstScore < grid[currentRow-1][currentCol].firstScore )//!considered.contains(grid[currentRow-1][currentCol]))
							{
								
								if(PQ.contains( grid[currentRow-1][currentCol]))
								{
									PQ.remove(grid[currentRow-1][currentCol]);
								}
								
								grid[currentRow-1][currentCol].path.clear();
								
								for(int i = 0 ; i <current.path.size() ; i++)
								{
									grid[currentRow-1][currentCol].path.add(current.path.get(i));
								}
								grid[currentRow-1][currentCol].path.add(current.rect);
								
								grid[currentRow-1][currentCol].firstScore = current.firstScore+1;
								grid[currentRow-1][currentCol].secondScore = secondScore((int)(grid[currentRow-1][currentCol].rect.getX()) , (int)(grid[currentRow-1][currentCol].rect.getY()) ,(int) (e.rect.getX() ),(int)( e.rect.getY()));
								grid[currentRow-1][currentCol].totalScore = grid[currentRow-1][currentCol].firstScore + grid[currentRow-1][currentCol].secondScore;
								
								if(grid[currentRow-1][currentCol].equals(e))
									{
										found = true;
										finishAlg(grid[currentRow-1][currentCol].path , g2d );
										break;
									}else
									{
										g2d.setColor(new Color(235,117,0));
										g2d.fill(grid[currentRow-1][currentCol].rect);
										PQ.add(grid[currentRow-1][currentCol]);
									}
							}
						}
					
					}
				
						
						
			try
			{
				TimeUnit.MILLISECONDS.sleep(20);
				
			}
			catch(Exception exce)
			{
				System.out.println("ljdlawjd");
			}
			
			if(!current.equals(s))
						  	{
								g2d.setColor(new Color(235,117,0));
								g2d.fill(current.rect);
							}
						System.out.println("");	
						System.out.println("");	
			

			}
			
			
		}
		
		public static void finishAlg(ArrayList<Rectangle> path , Graphics2D g2d)
		{
			for(Rectangle r : path)
			{
				
				
				System.out.print((int)(r.getX())/20+" ");
				System.out.print((int)(r.getY()-30)/20);
				System.out.println("");
				
				g2d.setColor(endColor);
				g2d.fill(r);
			}
			System.out.println("");
		}
		
		public static boolean validNeighbor(int row , int col)
		{
			
			if( (row>=0 && row<38) && (col>=0 && col<40))
			{
				return true;
			}else return false;
		}
	
}

class Node implements Comparable<Node>
{
	Rectangle rect;
	ArrayList<Rectangle> path = new ArrayList<>();
	int firstScore;
	int secondScore;
	int totalScore;
	
	Node(){}
	
	Node(int f , int s , int t , Rectangle r)
	{
		this.firstScore = f;
		this.secondScore = s;
		this.totalScore = t;
		this.rect = r;
	}
	
	 @Override
    public int compareTo(Node n) {
        if(this.totalScore == n.totalScore)
		{
			if(this.secondScore > n.secondScore)
			{
				return 1;
			}else if(n.secondScore > this.secondScore) return -1;
			else return 0;
			
		}
		else if(this.totalScore > n.totalScore)return 1;
		else return -1;
    }
	
}






class Window extends JFrame
{
	
	Window(String name )
	{
		super(name);
		
	}
	
}


class winContainer extends JComponent
{
	Node [][] grid ;
	
	
	
	winContainer(Node [][] grid, int w , int h)
	{
		this.grid = grid;
		this.setSize(w,h);
	}
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		drawGrid(grid,g2d);
			
		
	}
	public void drawGrid(Node[][] grid , Graphics2D g2d)
	{
		for(Node[] row : grid)
		{
			for(Node node : row)
			{
				g2d.draw(node.rect);
			}
		}
	}
	
}


