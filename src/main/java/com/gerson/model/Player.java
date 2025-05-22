package com.gerson.model;

import java.io.PrintStream;

/**
 * Representa a un jugador en la carrera.
 * Contiene su identificador, nombre, progreso, clics reportados y canal de
 * salida.
 */
public class Player {
  private final int id;
  private String name;
  private int progress;
  private int reportedClicks;
  private final PrintStream output;

  /**
   * Constructor del jugador. Inicializa su identificador, canal de salida y
   * nombre predeterminado.
   * 
   * @param id     identificador único del jugador
   * @param output canal de salida para enviar mensajes al jugador
   */
  public Player(int id, PrintStream output) {
    this.id = id;
    this.output = output;
    this.progress = 0;
    this.reportedClicks = 0;
    this.name = "Jugador " + id;
  }

  /**
   * Devuelve el identificador único del jugador.
   * 
   * @return id del jugador
   */
  public int getId() {
    return id;
  }

  /**
   * Devuelve el nombre del jugador.
   * 
   * @return nombre del jugador
   */
  public String getName() {
    return name;
  }

  /**
   * Establece el nombre del jugador.
   * 
   * @param name nuevo nombre del jugador
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Devuelve el progreso actual del jugador en la carrera (0 a 100).
   * 
   * @return porcentaje de progreso del jugador
   */
  public int getProgress() {
    return progress;
  }

  /**
   * Aumenta el progreso del jugador sin superar el 100%.
   * 
   * @param amount cantidad a avanzar
   */
  public void advance(int amount) {
    this.progress = Math.min(100, this.progress + amount);
  }

  /**
   * Devuelve la cantidad de clics reportados por el jugador.
   * 
   * @return número de clics
   */
  public int getReportedClicks() {
    return reportedClicks;
  }

  /**
   * Establece la cantidad de clics reportados por el jugador.
   * 
   * @param reportedClicks número de clics a registrar
   */
  public void setReportedClicks(int reportedClicks) {
    this.reportedClicks = reportedClicks;
  }

  /**
   * Devuelve el canal de salida asociado al jugador.
   * 
   * @return PrintStream para enviar mensajes
   */
  public PrintStream getOutput() {
    return output;
  }
}