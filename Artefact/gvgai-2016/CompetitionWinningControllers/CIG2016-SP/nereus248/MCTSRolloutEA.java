package nereus248;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import ontology.Types.ACTIONS;

public class MCTSRolloutEA {

	Random rnd;
	
	MCTSTree mctsTree;
	int pathLengths;
	
	int maxPopulationSize = 8;
	int worstIndex = -1;
	int currentIndex = -1;
	int bestIndex;
	
	double bestScore = -Double.MAX_VALUE;
	double worstScore = Double.MAX_VALUE;
	
	double mutationChance = 0.3;
	double recombinationChance = 0.3;

	ArrayList<EAPathItem> populationArray;
	
	// Construct a new Population for some node (actually only used for the very first root)
	MCTSRolloutEA(int length, Random rnd, MCTSTree mctsTree) {
		this.rnd = rnd;
		this.mctsTree = mctsTree;
		this.pathLengths = length;

		populationArray = new ArrayList<EAPathItem>();
		while (populationArray.size() < 2) {
			populationArray.add(createRandomPathItem(pathLengths));
		}
	}
	
	/**
	 * Construct a new Population based on the parents 'template' population and the action that lead to this node.
	 * @param template
	 * @param firstAction
	 */
	MCTSRolloutEA(MCTSRolloutEA template, ACTIONS firstAction) {
		this.rnd = template.rnd;
		this.mctsTree = template.mctsTree;
		this.pathLengths = template.pathLengths;
		
		this.populationArray = new ArrayList<EAPathItem>();
		// Take those paths from the old MCTSRolloutEA that match the first action
		for(EAPathItem item : template.populationArray) {
			if (item.path.get(0) == firstAction) {
				EAPathItem newItem = item.copy();
				newItem.chopFront();
				newItem.extend(MCTSTree.availableActions[rnd.nextInt(MCTSTree.numAvailableActions)]);
				if (newItem.score > bestScore) {
					bestScore = newItem.score;
					bestIndex = populationArray.size();
				}
				populationArray.add(newItem);
			}
		}
		while (populationArray.size() < 2) {
			populationArray.add(createRandomPathItem(pathLengths));
		}
	}
	
	public void getEARolloutPathsFromChildren(MCTSNode[] children) {
		int n = children.length;
		populationArray.clear();
		MCTSNode child;
		for (int i = 0; i < n; i++) {
			child = children[i];
			if (child == null)
				continue;
			MCTSRolloutEA childPopulation = child.mctsRolloutEA;
			populationArray.clear();
			ACTIONS lastAction = child.thisNodesAction;
			for (EAPathItem item : childPopulation.populationArray) {
				EAPathItem newItem = item.copy();
				newItem.prependAction(lastAction);
				populationArray.add(newItem);
			}
		}
		// Sort all action sequences according to score with the highest score first
		Collections.sort(populationArray, Collections.reverseOrder());
		// Take the only the first few action sequences
		populationArray.subList(0, maxPopulationSize);
	}
	
	/**
	 * Evolves the selected EAPahtItem and returns the mutated one as a new instance
	 * @param item
	 * @return
	 */
	private EAPathItem evolve(final EAPathItem item) {

		EAPathItem mutatedItem = item.copy();
		if (rnd.nextDouble() < 0.3) {
			// Recombinate with random action sequence of the population
			EAPathItem recombinationPartner = populationArray.get(rnd.nextInt(populationArray.size()));
			for(int i = 0; i < pathLengths; i++) {
				if(rnd.nextDouble() < recombinationChance) {
					mutatedItem.mutateLocation(i, recombinationPartner.path.get(i));
				}
			}
		} else {
			// Mutate 'item' at random locations
			for(int i = 0; i < pathLengths; i++) {
				if(rnd.nextDouble() < mutationChance) {
					mutatedItem.mutateLocation(i, MCTSTree.availableActions[rnd.nextInt(MCTSTree.numAvailableActions)]);
				}
			}
		}
		return mutatedItem;
	}
	
	/**
	 * Returns a mutation of the currently best action sequence and adds this mutation to the population
	 * by discarding the action sequence with the worst score so far. Once the rollout has been performed
	 * with the returned EAPathItem, the update() function has to be called.
	 * @return
	 */
	public EAPathItem getRecombinationOfBestPath() 
	{
		if (bestIndex == -1) {
			bestIndex = rnd.nextInt(populationArray.size());
		}
		EAPathItem newItem = evolve(populationArray.get(bestIndex));
		if (populationArray.size() >= maxPopulationSize) {
			// Remove worst one
			double minScore = Double.MAX_VALUE;
			int minIndex = 0;
			for(int i = 0; i < maxPopulationSize; i++) {
				if (populationArray.get(i).score < minScore) {
					minScore = populationArray.get(i).score;
					minIndex = i;
				}
			}
			currentIndex = minIndex;
			populationArray.set(minIndex, newItem);
			
		} else {
			currentIndex = populationArray.size();
			populationArray.add(newItem);
		}
		
		return newItem;
	}
	
	/**
	 * Has to be called after the rollout has been evaluated. Updates the scores of the 
	 * population and looks for a possibly new best action sequence
	 */
	public void update() {
		if (currentIndex == -1) {
			return;
		}
		double newScore = populationArray.get(currentIndex).score;
		if(newScore > bestScore) {
			bestIndex = currentIndex;
			bestScore = newScore;
		} 
		currentIndex = -1;
	}
	
	/**
	 * Creates a random action sequence of the given length.
	 * @param length
	 * @return
	 */
	private EAPathItem createRandomPathItem(int length) {
		EAPathItem item = new EAPathItem();
		item.path = new ArrayList<ACTIONS>(length);
		item.score = -Double.MAX_VALUE;
		for (int i = 0; i < length; i++){
			item.path.add(MCTSTree.availableActions[rnd.nextInt(MCTSTree.numAvailableActions)]);
		}
		return item;
	}
	
	/**
	 * Sets the height of the action sequence that should be returned by the next calls of 'getRecombinationOfBestPath()'
	 * @param height
	 */
	public void setHeight(int height) {
		if (height > pathLengths) {
			for (EAPathItem item : populationArray) {
				int diff = height - item.path.size();
				for (int i = 0; i < diff; i++) {
					item.extend(MCTSTree.availableActions[rnd.nextInt(MCTSTree.numAvailableActions)]);
				}
			}
		}
		pathLengths = height;
	}

}
