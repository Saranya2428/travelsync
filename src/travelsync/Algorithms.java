package travelsync;
import java.util.*;
final class Algorithms {
 static List<Integer> kmp(String t,String p){List<Integer> out=new ArrayList<>();if(p.isEmpty())return out;int[] l=new int[p.length()];for(int i=1,j=0;i<p.length();)if(p.charAt(i)==p.charAt(j))l[i++]=++j;else if(j>0)j=l[j-1];else l[i++]=0;for(int i=0,j=0;i<t.length();)if(t.charAt(i)==p.charAt(j)){i++;j++;if(j==p.length()){out.add(i-j);j=l[j-1];}}else if(j>0)j=l[j-1];else i++;return out;}
 static boolean kmpContains(String t,String p){return p.isEmpty()||!kmp(t,p).isEmpty();}
 static int distance(String a,String b){int[][] d=new int[a.length()+1][b.length()+1];for(int i=0;i<=a.length();i++)d[i][0]=i;for(int j=0;j<=b.length();j++)d[0][j]=j;for(int i=1;i<=a.length();i++)for(int j=1;j<=b.length();j++)d[i][j]=Math.min(Math.min(d[i-1][j]+1,d[i][j-1]+1),d[i-1][j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1));return d[a.length()][b.length()];}
 static String route(){return "Dijkstra demo: Vijayawada -> Tirupati, illustrative distance 410 km (matrix demo).";}
 static int flow(){int[][] c={{0,8,5,0,0,0},{0,0,3,4,0,0},{0,0,0,2,6,0},{0,0,0,0,3,7},{0,0,0,0,0,9},{0,0,0,0,0,0}},r=new int[6][6];for(int i=0;i<6;i++)r[i]=c[i].clone();int total=0;while(true){int[] p=new int[6],q=new int[6];Arrays.fill(p,-1);p[0]=0;int h=0,t=0;q[t++]=0;while(h<t&&p[5]<0){int u=q[h++];for(int v=0;v<6;v++)if(p[v]<0&&r[u][v]>0){p[v]=u;q[t++]=v;}}if(p[5]<0)break;int f=999;for(int v=5;v!=0;v=p[v])f=Math.min(f,r[p[v]][v]);for(int v=5;v!=0;v=p[v]){r[p[v]][v]-=f;r[v][p[v]]+=f;}total+=f;}return total;}
 static String tsp(){int[][] w={{0,10,15,20,18},{10,0,35,25,12},{15,35,0,30,10},{20,25,30,0,14},{18,12,10,14,0}};int n=5,N=1<<n,inf=999999;int[][] d=new int[N][n];for(int[] a:d)Arrays.fill(a,inf);d[1][0]=0;for(int m=1;m<N;m++)if((m&1)>0)for(int u=0;u<n;u++)for(int v=0;v<n;v++)if((m&(1<<u))>0&&(m&(1<<v))==0)d[m|1<<v][v]=Math.min(d[m|1<<v][v],d[m][u]+w[u][v]);int b=inf;for(int u=1;u<n;u++)b=Math.min(b,d[N-1][u]+w[u][0]);return "Held-Karp sample best tour cost: "+b;}
 static String sortDemo(){int[] a={42,7,19,3,28,11,5,31,2};quick(a,0,a.length-1);return Arrays.toString(a);}
 static void quick(int[] a,int l,int h){if(l>=h)return;int p=l+new Random().nextInt(h-l+1),t=a[p];a[p]=a[h];a[h]=t;int x=a[h],i=l;for(int j=l;j<h;j++)if(a[j]<=x){t=a[i];a[i++]=a[j];a[j]=t;}t=a[i];a[i]=a[h];a[h]=t;quick(a,l,i-1);quick(a,i+1,h);}
 static List<String> sample(List<String> a,int k){List<String> r=new ArrayList<>();Random x=new Random();for(int i=0;i<a.size();i++)if(i<k)r.add(a.get(i));else{int j=x.nextInt(i+1);if(j<k)r.set(j,a.get(i));}return r;}
}
