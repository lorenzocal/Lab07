package it.polito.tdp.poweroutages.DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import it.polito.tdp.poweroutages.model.Area;
import it.polito.tdp.poweroutages.model.EventType;
import it.polito.tdp.poweroutages.model.Nerc;
import it.polito.tdp.poweroutages.model.PowerOutage;
import it.polito.tdp.poweroutages.model.Responsible;
import it.polito.tdp.poweroutages.model.Tag;

public class PowerOutageDAO {
	
	private AreaDAO areaDAO;
	private Map<Integer, Area> areaIdMap;
	private EventTypeDAO eventTypeDAO;
	private Map<Integer, EventType> eventTypeIdMap;
	private NercDAO nercDAO;
	private Map<Integer, Nerc> nercIdMap;
	private ResponsibleDAO responsibleDAO;
	private Map<Integer, Responsible> responsibleIdMap;
	private TagDAO tagDAO;
	private Map<Integer, Tag> tagIdMap;
	

	public PowerOutageDAO() {
		super();
		this.areaDAO = new AreaDAO();
		this.eventTypeDAO = new EventTypeDAO();
		this.nercDAO = new NercDAO();
		this.responsibleDAO = new ResponsibleDAO();
		this.tagDAO = new TagDAO();
		this.areaIdMap = this.areaDAO.getAreaIdMap();
		this.eventTypeIdMap = this.eventTypeDAO.getEventTypeIdMap();
		this.nercIdMap = this.nercDAO.getNercIdMap();
		this.responsibleIdMap = this.responsibleDAO.getResponsibleIdMap();
		this.tagIdMap = this.tagDAO.getTagIdMap();
	}
	
	public Double durataDisservizioOre(LocalDateTime ldt1, LocalDateTime ldt2) {
		Duration duration = Duration.between(ldt1, ldt2);
		return (double) duration.toSeconds()/3600;
	}
	
	public Map<Integer, Area> getAreaIdMap() {
		return areaIdMap;
	}


	public Map<Integer, EventType> getEventTypeIdMap() {
		return eventTypeIdMap;
	}


	public Map<Integer, Nerc> getNercIdMap() {
		return nercIdMap;
	}


	public Map<Integer, Responsible> getResponsibleIdMap() {
		return responsibleIdMap;
	}


	public Map<Integer, Tag> getTagIdMap() {
		return tagIdMap;
	}


	public LocalDateTime convertTimestampToLocalDateTime(Timestamp timestamp) {
	    return timestamp.toLocalDateTime();
	}
	
	public Map<Integer, PowerOutage> getPowerOutageIdMap() {

		String sql = "SELECT * FROM poweroutages";
		Map<Integer, PowerOutage> outageIdMap = new HashMap<Integer, PowerOutage>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				
				Integer id = res.getInt("id");
				Integer eventTypeId = res.getInt("event_type_id");
				Integer tagId = res.getInt("tag_id");
				Integer areaId = res.getInt("area_id");
				Integer nercId = res.getInt("nerc_id");
				Integer responsibleId = res.getInt("responsible_id");
				Integer customersAffected = res.getInt("customers_affected");
				LocalDateTime dateEventBegan = this.convertTimestampToLocalDateTime(res.getTimestamp("date_event_began"));
				LocalDateTime dateEventFinished = this.convertTimestampToLocalDateTime(res.getTimestamp("date_event_finished"));
				Integer demandLoss = res.getInt("demand_loss");
				Double durataDisservizio = this.durataDisservizioOre(dateEventBegan, dateEventFinished);
				
				PowerOutage po = new PowerOutage(
						id,
						this.eventTypeIdMap.get(eventTypeId),
						this.tagIdMap.get(tagId),
						this.areaIdMap.get(areaId),
						this.nercIdMap.get(nercId),
						this.responsibleIdMap.get(responsibleId),
						customersAffected,
						dateEventBegan,
						dateEventFinished,
						demandLoss,
						durataDisservizio);
				
				outageIdMap.put(id, po);
			}

			conn.close();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return outageIdMap;
	}
	

}
