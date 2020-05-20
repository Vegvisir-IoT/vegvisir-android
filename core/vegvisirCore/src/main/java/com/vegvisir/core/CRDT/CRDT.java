package com.vegvisir.core.CRDT;

import java.util.*;

public class CRDT<transactionID, itemType> {

	//numSets represents the number of sets in the NP Set for each transaction.
	private int numSets;
	// hasRemove is true if the highest priority set is a remove set, false
	// otherwise.
	private boolean hasRemove;
	// A hashmap mapping transaction IDs to the NP Set for that ID.
	private HashMap<transactionID,ArrayList<HashSet<itemType>>> nPSets;
	// The NP Set of Top.
	private ArrayList<HashSet<itemType>> topNPSet;
	// The set of dependencies of topDeps
	private HashSet<transactionID> topDeps = new HashSet<>();;

	public CRDT(int numSets, boolean hasRemove) {
		this.numSets = numSets;
		this.hasRemove = hasRemove;
		this.nPSets = new HashMap<>();
		this.topNPSet = new ArrayList<>(this.numSets);
	}

	/**
	 * @param originalNPSet - the original NP Set of the relevant transaction
	 * @param dependencies - a set of transactions
	 * @return - a new NP Set such that for each i from 0 to n, the new
	 * NP Set's ith set is the union of the ith sets of the NP Set
	 * corresponding to each of the dependencies
	 */
	public ArrayList<HashSet<itemType>> updateNPSetWithDeps(
			ArrayList<HashSet<itemType>> originalNPSet,
			HashSet<transactionID> dependencies) {
		for (transactionID dep: dependencies) {
			if (nPSets.containsKey(dep)) {
				ArrayList<HashSet<itemType>> depNPSet = this.nPSets.get(dep);
				for (int i = 0; i < this.numSets; i++) {
					originalNPSet.set(i, depNPSet.get(i));
				}
			}
		}

		return originalNPSet;
	}

	/**
	 * @param transactionKey - the ID of the transaction to be added
	 * @param dependencies - the set of dependencies of the transaction
	 * @param transactionType - an int from 0 to n-1 that represents the type
	 * of transaction to be carried out (in increasiing order of priority).
	 * @param item - the item that the transaction is on, of type itemType.
	 */
	public void addTransaction(transactionID transactionKey,
							   HashSet<transactionID> dependencies,
							   int transactionType, itemType item) {

		ArrayList<HashSet<itemType>> txNPSet = this.updateNPSetWithDeps(
				new ArrayList<>(), dependencies);

		for (int i = 0; i < this.numSets; i++) {
			if (i == transactionType) {
				txNPSet.get(i).add(item);
			}
			else {
				txNPSet.get(i).remove(item);
			}
		}

		this.nPSets.put(transactionKey, txNPSet);

		this.topDeps.add(transactionKey);

		for (transactionID dep: dependencies) {
			this.topDeps.remove(dep);
		}

		this.topNPSet = this.updateNPSetWithDeps(this.topNPSet, this.topDeps);

	}

	/**
	 * @return - The current items for eech priority by increasing priority,
	 * based off of the NP Set for top.
	 */
	public ArrayList<HashSet<itemType>> getItems() {
		ArrayList<HashSet<itemType>> items = new ArrayList<>(this.numSets);
		for (int i = 0; i < this.numSets; i++) {
			HashSet<itemType> currItems = new HashSet<>();
			currItems.addAll(topNPSet.get(i));
			for (int j = i+1; j < this.numSets; j++) {
				currItems.removeAll(topNPSet.get(j));
			}
			items.set(i,currItems);
		}

		if (this.hasRemove) {
			items.set(this.numSets - 1, new HashSet<itemType>());
		}
		else {
			items.set(this.numSets - 1, topNPSet.get(this.numSets - 1));
		}

		return items;
	}

}