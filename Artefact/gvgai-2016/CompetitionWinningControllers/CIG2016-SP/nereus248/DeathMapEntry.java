package nereus248;

public class DeathMapEntry {
	
	public DeathMapEntry() {
		nofDeaths = 0;
		nofVisits = 0;
	}
	
	int nofDeaths;
	int nofVisits;
	
	double getLeathality() {
		return nofDeaths/nofVisits;
	}
}
