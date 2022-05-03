/*
edge cases
-start cell and end cell can be the same cell (not handled)
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

					
					
					if( handleNeighbor(currentRow ,currentCol-1 , g2d , newFirstScore , PQ , current , found ,  e) )break;
					
					if(handleNeighbor(currentRow ,currentCol+1 , g2d , newFirstScore , PQ , current , found,  e) )break;
					
					if(handleNeighbor(currentRow+1 ,currentCol , g2d , newFirstScore , PQ , current , found,  e) )break;
					
					if(handleNeighbor(currentRow-1 ,currentCol , g2d , newFirstScore , PQ , current , found,  e) )break;
						
						
			try
			{
				TimeUnit.MILLISECONDS.sleep(0);
				
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
		
		public static boolean handleNeighbor(int row , int col  , Graphics2D g2d , int newFirstScore , PriorityQueue < Node> PQ , Node current , boolean found , Node e)
		{
			if(validNeighbor(row, col)) // if up exists
					{
						
						if(!barriers.contains(grid[row][col].rect)) // if its not a barrier
						{
							
							if(newFirstScore < grid[row][col].firstScore )//!considered.contains(grid[currentRow-1][currentCol]))
							{
								
								if(PQ.contains( grid[row][col]))
								{
									PQ.remove(grid[row][col]);
								}
								
								grid[row][col].path.clear();
								
								for(int i = 0 ; i <current.path.size() ; i++)
								{
									grid[row][col].path.add(current.path.get(i));
								}
								grid[row][col].path.add(current.rect);
								
								grid[row][col].firstScore = current.firstScore+1;
								grid[row][col].secondScore = secondScore((int)(grid[row][col].rect.getX()) , (int)(grid[row][col].rect.getY()) ,(int) (e.rect.getX() ),(int)( e.rect.getY()));
								grid[row][col].totalScore = grid[row][col].firstScore + grid[row][col].secondScore;
								
								if(grid[row][col].equals(e))
									{
										found = true;
										finishAlg(grid[row][col].path , g2d );
										
									}else
									{
										g2d.setColor(new Color(235,117,0));
										g2d.fill(grid[row][col].rect);
										PQ.add(grid[row][col]);
									}
							}
						}
					
					}
					return found;
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


