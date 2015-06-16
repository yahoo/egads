package com.yahoo.egads.data;

public interface ModelStore {
	public void storeModel(String tag, Model m);
	public Model retrieveModel (String tag);
}
