package com.example.telitodev.dto;

import java.util.List;
import java.util.Map;


public class DashboardPoDTO {


    private int totalProyectos;
    private int apisUtilizadas;
    private long totalLlamadas;
    private double latenciaPromedio;
    private int totalErrores;
    private double costoTotal;


    private Map<String, Long> llamadasPorDia;
    private Map<String, Long> roadmapStatus;


    private List<FeedbackRecienteDTO> feedbackRecientes;
    private List<BacklogResumenDTO> backlogActivos;



    public DashboardPoDTO() { }




    public int getTotalProyectos() { return totalProyectos; }
    public void setTotalProyectos(int totalProyectos) { this.totalProyectos = totalProyectos; }
    public int getApisUtilizadas() { return apisUtilizadas; }
    public void setApisUtilizadas(int apisUtilizadas) { this.apisUtilizadas = apisUtilizadas; }
    public long getTotalLlamadas() { return totalLlamadas; }
    public void setTotalLlamadas(long totalLlamadas) { this.totalLlamadas = totalLlamadas; }
    public double getLatenciaPromedio() { return latenciaPromedio; }
    public void setLatenciaPromedio(double latenciaPromedio) { this.latenciaPromedio = latenciaPromedio; }
    public int getTotalErrores() { return totalErrores; }
    public void setTotalErrores(int totalErrores) { this.totalErrores = totalErrores; }
    public double getCostoTotal() { return costoTotal; }
    public void setCostoTotal(double costoTotal) { this.costoTotal = costoTotal; }
    public Map<String, Long> getLlamadasPorDia() { return llamadasPorDia; }
    public void setLlamadasPorDia(Map<String, Long> llamadasPorDia) { this.llamadasPorDia = llamadasPorDia; }
    public Map<String, Long> getRoadmapStatus() { return roadmapStatus; }
    public void setRoadmapStatus(Map<String, Long> roadmapStatus) { this.roadmapStatus = roadmapStatus; }
    public List<FeedbackRecienteDTO> getFeedbackRecientes() { return feedbackRecientes; }
    public void setFeedbackRecientes(List<FeedbackRecienteDTO> feedbackRecientes) { this.feedbackRecientes = feedbackRecientes; }
    public List<BacklogResumenDTO> getBacklogActivos() { return backlogActivos; }
    public void setBacklogActivos(List<BacklogResumenDTO> backlogActivos) { this.backlogActivos = backlogActivos; }
}
