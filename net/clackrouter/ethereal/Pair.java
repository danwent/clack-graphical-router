package net.clackrouter.ethereal;

/* I could add caching so pairs aren't made if they are already present but that is for later */

public class Pair {

	public String first;
	public Integer second;
	
	public Pair(String a, Integer b) { first = a; second = b; }
	
	
	
	public boolean equals(Object o) {
		if (o instanceof Pair) {
			Pair p = (Pair) o;
			if (p != null)
				if (first == null || first.equals(p.first))
					if (second == null || second.equals(p.second)) return true;
			}
		return false;
	}
	
	/**
	 * hashCode that combines two strings
	 * @return a hash code value on the pair of strings.
	 */
	public int hashCode()
	   {
	   int result = 17;
	   if (first != null)
		   result = 37 * result + first.hashCode();
	   if (second != null)
		   result = 37 * result + second.hashCode();
	   
	   // etc for all fields in the object
	   return result;
	   }
	
	public String toString() { return "(" + first + ", " + second + ")"; }
	
}
