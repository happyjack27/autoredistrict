FULLY AUTOMATED REDISTICTING SOFTWARE
written in java, open source.

HOW TO RUN:

Requires Java Runtime Environment (JRE) version 7 or greater.

Open a command prompt. Change to the directory containing the jar:
```
java -jar autoredistrict.jar -Xmx4096M -Xms1024M
```

```autoredistrict.jar``` is in the ```jar``` folder along with a script file for windows and one for linux/mac.
The -Xmx4096M -Xms1024M arguments tell java to reserve 1GB of memory, and allow additional allocation up to 4GB.

HOW TO BUILD AND RUN:

Requires Java Development Kit (JDK) version 7 or greater.
Open a command prompt. Change to the directory that you cloned the repo to. Make a ```bin``` directory here. Run:

```
javac -encoding UTF-8 -d bin -cp 'src:jcom.jar' src/ui/Applet.java
```

Now create a jar by changing to the ```bin``` directory and running:
```
jar cf autoredistrict.jar .
```

To start the newly-compiled app from the bin directory, run:
```
java -Xmx4096M -Xms1024M -classpath autoredistrict.jar:../jcom.jar ui/Applet
```

CONTRIBUTION GUIDELINES:
* I won't merge anything that can be abused - that makes it so the program can be used to gerrymander.
it is currently impossible to use the program to gerrymander and it will stay that way.  this means that an otherwise "good" feature might get rejected - so be it.  this is the #1 rule and it trumps all other considerations.
* I'll only merge it if it's a definite improvement.
* keep the code simple and clean - i won't merge if it makes the code unneccessarily complicated or difficult to compile.  (e.g. tons of dependancies)  i don't want code bloat, i want to keep it easy to maintain.

https://travis-ci.org/happyjack27/autoredistrict.svg?branch=master

TODO:
* is .dbf save working?  seems to not be - need to save column widths and types!
* make project files load/save other files relative to woring directory of project file. (and make taht the default in file selection dialog)
* make able to run gui-less from the command line
* means need to have runtime (or generation count), save destination

* add multiple demographics and elections

* beat or integrate with: http://www.caliper.com/mtrnews/clients.htm

* add layer viewer tree view on right
* add file system explorer on left, with a default workspace
* ?add aggregation / deaggregation / redistribution?
* http://www.caliper.com/RedistrictingFeatures.htm
* advanced global metrics 
- show population standard deviation, mean, and max deviation,
- show different compactness scores.
--polsby-popper, schwaretzberg, convex hull, reock, squared length per area
* show global scores by sliders
* show global score movement rate by sliders
* tooltip hints

pct of area - total population - old - normalize to current population
pct of area - total population - new

load into temporary geography file (each get its own min/max)
* do by dots? original_geode[][], new_geode[][] 

import data - different shapefile - "import incongruous data"
- by population density
1. align both maps
0. collect population columns and columns to import.
2. cut space up into tiny grid (squares)
3. find population density of each region (divide by area)
4. allocate population to squares based on area intersecting. ( 
polygon clipping
intersecting area polygon
http://rosettacode.org/wiki/Sutherland-Hodgman_polygon_clipping#Java
)
5. aggregate data into working regions, totaling population
6. then adjust to match new population. (multiply by working pop / imported pop)


DONE:
* save project
* import result column
* export all columns to .txt. or .csv - likewise, import
* export all columns, including new district, to .dbf.
* move population column selection on to main view (and remember it) (and remember from layer selection)
* remember demographics columns and show on layer selection


--------------

 
* allow feature selection (for choosing population, vote results, etc.)
* add ability to lock/unlock precincts together
* add ability to lock/unlock precincts to certain districts
* to read esri shapefile directly:
** add in https://sourceforge.net/projects/javashapefilere/
** add in http://code.google.com/p/jdbf/source/checkout
** create reader option
* how to handle ward-level data? (super-fine)
* check for ideas on what to implement: http://www.fairvote.org/research-and-analysis/redistricting/redistricting-reform/model-state-redistricting-reform-criteria/
** local representativeness score - based on other factors. - local competitiveness? (for responsiveness)

a.    A representative plan is one where racial groups and other communities of interest are able to elect representatives in proportion to their percentage of the voting age population.
7.    District boundaries shall conform to the existing geographic boundaries of a county, city, or city and county, and shall preserve identifiable communities of interest to the greatest extent possible. A redistricting plan shall provide for the most whole counties and the fewest county fragments possible, and the most whole cities and fewest city fragments possible. For the purposes of this section, communities of interest are defined by similarities in social, cultural, ethnic, and economic interest, school districts, and other formal relationships between municipalities.

5.    Information concerning party registration and historical election returns shall only be used once a plan has been drawn, and shall only be used to test the plan for compliance with the stated goals of this article.


DONE:
* after loading, fill in ecology data structures.
* also initalize candidates in ecology on loading of election results. 
* write parts to adjust population, number of districts on evolve function
* write parts to start and stop evolution
* write parts to color code wards by district based on best scoring map. (make sure to invalidate and repaint each evolve iteration)
* add inverters for score components - in drop down menu.
* multithread testing, but reusable threads.
* add exporting of results ( simple tab deliminted: precinct - district)
* add listener to num of districts to adjust, should be a target and work together with evolve. 
* add zoom in feature
* remove population replaced slider
* add option to replace entire population (disables mutate all)
* add play and stop and reverse for evolution (remove invert)
* add border mutation rate to stats screen
* change color profile



DONE:
auto-annealing (instead of having to "drive" it) 
* way to auto-adjust border mutation based on scoring relative to past generation?
* * use mean expectation of winners as new border mutation (take that and average of last)
* * means have to store pre-mutation, state, and then count the fraction of mutated borders.
* * or just do it on the fly - num mutated, vs num total
* * then we add an option to turn on/off auto-annealing, and add a little tutorial about it.
* show annealing floor in graph.
* dont start annealing until hits 25% mutation

TODO:

* allow adjusting annealing floor rate/period

* add no splitting rule (optional per town?)
* implement import population
* use the TIGER data set 'cause that has no gaps - have to figure out how to overlay other gis file.
* add load district info from geojson (they can choose the property, shift from 1-index)
*  add screen for loading different data elements from geojson properties
* add save district info to geojson (they can choose the property, shift to 1-index)
* implement show district labels
* create help text for all of the menu items, sliders, etc.
* add tooltips for all of the menu items, sliders, etc.
* move algorithm pieces into advanced help
* create walk-through
* create explanation of how to convert to/from geojson (ogr2ogr)
* talk about gaps, and how to resolve them (use tiger data)
* talk about how to combine data
* add command line stuff for scripting
* rename all labels such as "compactness" so they all use the same naming - use the naming for e.g. "border length" and "x imbalance"
* add saving and loading of history graph
* add forcing precincts to certain district, locking precinct districts so they dont change
* _ make option to change population balance to minimize pct difference between largest and smallest population
* show pct diff between largest and smallest pop.  must be w/in 10%  also show average deviation
* allow to chose method - bits - standard deviation - min. and max
* ability to set hard reject rules - adding a penalty of say 10 if fails.
* - implement federal criteria - http://redistricting.lls.edu/where.php#section2

new ui:
* show what's loaded ( geography, census/population, elections, districts)
* change menu to import / export?
* add sample to load and run (relatively small) 
* live graphs

=================    
RESULT SCORING IN DEPTH
-------------------
Initial loading
* unique vertexes are collected
* unique edges between vertexes are collected, along with what polygons are on each side of the edge.
* using these edges, each polygon collects all of its neighbors into a list, along with the total length of edges shared with that neighbor.
         
census and election data
* population and vote totals for each polgon are attached to the polygon.

Compactness (border length)
------------------
The total of length of all edges that have a different district on each side is accumulated.

	class DistrictMap {
	    double getEdgeLength() {
	        double length = 0;
	        for( ward b : wards) {
	        	int d1 = ward_districts[b.id];
	        	for( int i = 0; i < b.neighbor_lengths.length; i++) {
	        		int b2id = b.neighbors.get(i).id;
	            	int d2 = ward_districts[b2id];
	            	if( d1 != d2) {
	            		length += b.neighbor_lengths[i];
	            	}
	        	}
	        }
	        return length;
	    }
	}


Connectedness
------------------
Each district is broken up into self-connected regions. The total population for each such region is accumulated from the poplygon populationos.
The total population in the highest such region is subtracted from the total population in all regions combined, giving the toal population _not_ in the largest region.
This is the "disconnected population".

	class DistrictMap {
        double disconnected_pops = 0;
        if( Settings.disconnected_population_weight > 0) {
            for(District district : districts) {
            	//int count = district.getRegionCount(ward_districts);
            	//System.out.println("region count: "+count);
            	//disconnected_pops += count;
                disconnected_pops += district.getPopulation() - district.getRegionPopulation(district.getTopPopulationRegion(ward_districts));
            }
        }
    }
    class District {
	    Vector<ward> getTopPopulationRegion(int[] ward_districts) {
	        Vector<Vector<ward>> regions = getRegions(ward_districts);
	        Vector<ward> high = null;
	        double max_pop = 0;
	        for( Vector<ward> region : regions) {
	            double pop = getRegionPopulation(region);
	            if( pop > max_pop || high == null) {
	                max_pop = pop;
	                high = region;
	            }
	        }
	        return high;
	    }
	    Vector<Vector<ward>> getRegions(int[] ward_districts) {
	        Hashtable<Integer,Vector<ward>> region_hash = new Hashtable<Integer,Vector<ward>>();
	        Vector<Vector<ward>> regions = new Vector<Vector<ward>>();
	        for( ward ward : wards) {
	            if( region_hash.get(ward.id) != null)
	                continue;
	            Vector<ward> region = new Vector<ward>();
	            regions.add(region);
	            addAllConnected(ward,region,region_hash,ward_districts);
	        }
	        return regions;
	    }
	    //recursively insert connected wards.
	    void addAllConnected( ward ward, Vector<ward> region,  Hashtable<Integer,Vector<ward>> region_hash, int[] ward_districts) {
	        if( region_hash.get(ward.id) != null)
	            return;
	        region.add(ward);
	        region_hash.put(ward.id,region);
	        for( ward other_ward : ward.neighbors) {
	        	if( ward_districts[other_ward.id] == ward_districts[ward.id]) {
	        		addAllConnected( other_ward, region, region_hash, ward_districts);
	        	}
	        }
	    }
	    double getRegionPopulation(Vector<ward> region) {
	        double population = 0;
	        if( region == null) {
	        	return 0;
	        }
	        for( ward ward : region) {
	        	if( ward.has_census_results) {
	        		population += ward.population;
	        	} else {
	            	for(Demographic p : ward.demographics) {
	            		population += p.population;
	            	}
	        	}
	        }
	        return population;
	    }
	}

         
Population balance
------------------
The population of each district is accumulated from the polygon populations.
Then the kullbach-leibler divergence of this from an equal distribution is calculated.  This is the "population imbalance".

	class DistrictMap {
        double population_imbalance = 0;
        if( Settings.population_balance_weight > 0 || Settings.voting_power_balance_weight > 0) {
            for(int i = 0; i < dist_pops.length; i++) {
            	if( districts.size() <= i) {
                    dist_pops[i] = 0;
            		
            	} else {
                    District district = districts.get(i);
                    dist_pops[i] = district.getPopulation();
            	}
                total_population += dist_pops[i];
            }
        	double rtotpop = 1.0/ total_population;
            for(int i = 0; i < dist_pops.length; i++) {
                dist_pop_frac[i] = dist_pops[i] * rtotpop;
            }

            double exp_population = total_population/(double)dist_pops.length;
            //System.out.println("exp. pop. "+exp_population);
            for( int i = 0; i < perfect_dists.length; i++) {
                perfect_dists[i] = exp_population;
            }
            population_imbalance = getKLDiv(perfect_dists,dist_pops,1);
        }
        public double getKLDiv(double[] p, double[] q, double regularization_factor) {
	    	boolean verbose = false;
	    	if( regularization_factor == 1.2 || regularization_factor == 0.01) {
	    		if( false) {
	    			verbose = true;
	    			System.out.println(" reg: "+regularization_factor);
	    		}
	    		//regularization_factor = 1;
	    	}
	    	if( verbose) {
	            for( int i = 0; i < p.length; i++) {
	            	System.out.println(" "+i+" p: "+p[i]+" q: "+q[i]);
	            }
	    		
	    	}
	        //regularize (see "regularization" in statistics)
	        for( int i = 0; i < p.length; i++)
	            p[i]+=regularization_factor;  
	        for( int i = 0; i < q.length; i++)
	            q[i]+=regularization_factor;  
	        
	        //get totals
	        double totp = 0;
	        for( int i = 0; i < p.length; i++)
	            totp += p[i];  
	        double totq = 0;
	        for( int i = 0; i < q.length; i++)
	            totq += q[i];  
	
	        //make same ratio.
	        double ratio = totp/totq;
	        for( int i = 0; i < q.length; i++)
	            q[i] *= ratio;  
	
	
	        //normalize
	        totp = 0;
	        for( int i = 0; i < p.length; i++)
	            totp += p[i];  
	        for( int i = 0; i < p.length; i++)
	            p[i] /= totp;
	        totq = 0;
	        for( int i = 0; i < q.length; i++)
	            totq += q[i];  
	        for( int i = 0; i < q.length; i++)
	            q[i] /= totq;
	
	        //get kldiv
	        double div = 0;
	        for( int i = 0; i < q.length; i++) {
	        	if( p[i] == 0) {
	        		continue;
	        	}
	        	double kl = p[i]*(Math.log(q[i]) - Math.log(p[i]));
	        	if( verbose) {
	        		System.out.println("i: "+i+" \tp: "+p[i]+" \tq:"+q[i]+" \tkl:"+kl);
	        	}
	            div += kl;
	        }
	        return -div;
	    }   
	}
