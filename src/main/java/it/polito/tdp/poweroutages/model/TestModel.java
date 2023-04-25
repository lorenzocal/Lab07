package it.polito.tdp.poweroutages.model;

import java.time.LocalDateTime;

public class TestModel {

	public static void main(String[] args) {
		
		Model model = new Model();
//		LocalDateTime ldt1 = LocalDateTime.parse("2019-04-28T22:32:38.536");
//		LocalDateTime ldt2 = LocalDateTime.parse("2017-01-14T15:32:56.000");
		//Double m = model.differenzaAnni(ldt2, ldt1);
		
		//System.out.println(m);
		model.avviaRicorsione(model.getNercIdMap().get(3), 4, 200);
		System.out.println(model.getMassimo());
		for (PowerOutage po : model.getWorstCase()) {
			System.out.println(po);
		}
	}

}
