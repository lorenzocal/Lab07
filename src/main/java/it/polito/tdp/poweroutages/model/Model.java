package it.polito.tdp.poweroutages.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import it.polito.tdp.poweroutages.DAO.PowerOutageDAO;


public class Model {
	
	private PowerOutageDAO podao;
	private Map<Integer, Area> areaIdMap;
	private Map<Integer, EventType> eventTypeIdMap;
	private Map<Integer, Nerc> nercIdMap;
	private Map<Integer, Responsible> responsibleIdMap;
	private Map<Integer, Tag> tagIdMap;
	private Map<Integer, PowerOutage> powerOutageIdMap;
	private int massimoPersoneCoinvolte = 0;
	private LinkedList<PowerOutage> worstCase;
	
	
	
	public Model() {
		this.podao = new PowerOutageDAO();
		this.areaIdMap = this.podao.getAreaIdMap();
		this.eventTypeIdMap = this.podao.getEventTypeIdMap();
		this.nercIdMap = this.podao.getNercIdMap();
		this.responsibleIdMap = this.podao.getResponsibleIdMap();
		this.tagIdMap = this.podao.getTagIdMap();
		this.powerOutageIdMap = this.podao.getPowerOutageIdMap();
		this.worstCase = new LinkedList<PowerOutage>();
	}
	
	public LinkedList<PowerOutage> filtroNerc(Nerc nerc, Map<Integer, PowerOutage> map) {
		
		LinkedList<PowerOutage> result = new LinkedList<PowerOutage>();
		
		for (Integer key : map.keySet()) {
			PowerOutage po = map.get(key);
			if (po.getNerc().equals(nerc)) {
				result.add(po);
			}
		}
		return result;
	}
	
	public Double differenzaAnni(LocalDateTime ldt1, LocalDateTime ldt2) {
		Duration duration = Duration.between(ldt1, ldt2);
		return (double) duration.toSeconds()/31556952;
	}

	public Double totaleOreDisservizio(LinkedList<PowerOutage> listaDisservizi) {
		double totale = 0;
		for (PowerOutage po : listaDisservizi) {
			totale = totale + po.getDurataDisservizioOre();
		}
		return totale;
	}
	
	public Double anniCoperti(LinkedList<PowerOutage> listaDisservizi) {
		LocalDateTime minAssoluto = listaDisservizi.get(0).getDateEventBegan();
		LocalDateTime maxAssoluto = listaDisservizi.get(0).getDateEventFinished();
		for (PowerOutage po : listaDisservizi) {
			LocalDateTime minLocale = po.getDateEventBegan();
			LocalDateTime maxLocale = po.getDateEventFinished();
			if (minLocale.isBefore(minAssoluto)) {
				minAssoluto = minLocale;
			}
			if (maxLocale.isAfter(maxAssoluto)) {
				maxAssoluto = maxLocale;
			}
		}
		return this.differenzaAnni(minAssoluto, maxAssoluto);
	}
	
	public Integer totalePersoneCoinvolte(LinkedList<PowerOutage> listaDisservizi) {
		Integer totale = 0;
		for (PowerOutage po : listaDisservizi) {
			totale = totale + po.getCustomersAffected();
		}
		return totale;
	}
	
	public void avviaRicorsione(Nerc nerc, Integer durataPolizza, Integer maxOreDisservizio) {
		
		LinkedList<PowerOutage> soluzioneParziale = new LinkedList<PowerOutage>();
		LinkedList<PowerOutage> tuttiDisserviziNerc = this.filtroNerc(nerc, this.powerOutageIdMap);
		
		for (PowerOutage po : tuttiDisserviziNerc) {
			if (po.getDurataDisservizioOre() <= maxOreDisservizio) {
				soluzioneParziale.add(po);
				ricerca(tuttiDisserviziNerc, soluzioneParziale, 1, durataPolizza, maxOreDisservizio);
			}
		}
		
	}
	
	public void ricerca(LinkedList<PowerOutage> tuttiDisserviziNerc, LinkedList<PowerOutage> soluzioneParziale, Integer livello, Integer durataPolizza, 
		Integer maxOreDisservizio) {
		
		for (PowerOutage po : tuttiDisserviziNerc) {
			
			if (!soluzioneParziale.contains(po)) {
				
				soluzioneParziale.add(po);
				
				Double anniCoperti = this.anniCoperti(soluzioneParziale);
				Double totaleOreDisservizio = this.totaleOreDisservizio(soluzioneParziale);
				Integer totalePersoneCoinvolte = this.totalePersoneCoinvolte(soluzioneParziale);
						
				if (anniCoperti <= durataPolizza && totaleOreDisservizio <= maxOreDisservizio) {
					ricerca(tuttiDisserviziNerc, soluzioneParziale, livello + 1, durataPolizza, maxOreDisservizio);
					if (totalePersoneCoinvolte>= this.massimoPersoneCoinvolte) {
						this.massimoPersoneCoinvolte = totalePersoneCoinvolte;
						this.worstCase.clear();
						this.worstCase.addAll(soluzioneParziale);
					}
//					System.out.println(soluzioneParziale);
//					System.out.println(this.totaleOreDisservizio(soluzioneParziale) + " | " + this.totalePersoneCoinvolte(soluzioneParziale));
				}
				soluzioneParziale.remove(soluzioneParziale.size() - 1);
			}	
		}	
	}	

	public LinkedList<PowerOutage> getWorstCase() {
		return worstCase;
	}

	public int getMassimo() {
		return massimoPersoneCoinvolte;
	}

	public Map<Integer, Nerc> getNercIdMap() {
		return nercIdMap;
	}
}
