package com.proyecto.pokemon_backend.service.logica;

import org.springframework.stereotype.Service;

import com.proyecto.pokemon_backend.model.enums.Estado;

import java.util.concurrent.ThreadLocalRandom; // Para el factor de Aleatoriedad

/**
 * Servicio que contiene todas las fórmulas matemáticas puras necesarias
 * para simular el combate (Daño, Experiencia, Precisión).
 */
@Service
public class CalculoService {

    // ------------------------------------------------------------------
    // 1. FÓRMULA DE DAÑO (Generación II/III)
    // ------------------------------------------------------------------

    /**
    * Calcula el daño final infligido por el atacante.
    * Basado en la fórmula: Daño = B*E*V*(((0.2 * N +1) * A * P)/ ( D * 25) +2))
    * N = Nivel del Pokémon que ataca.
    * A = Cantidad de ataque o ataque especial del Pokémon. Si el ataque que utiliza el Pokémon es físico se toma la cantidad de ataque y si es especial se toma la cantidad de ataque especial.
    * P = Poder del ataque, el potencial del ataque.
    * D = Cantidad de defensa del Pokémon RIVAL. Si el ataque que hemos utilizado es físico cogeremos la cantidad de defensa del Pokémon rival, si el ataque que hemos utilizado es especial, se coge la cantidad de defensa especial del Pokémon rival.
    * B = Bonificación. Si el ataque es del mismo tipo que el Pokémon que lo lanza toma un valor de 1.5 (stab), si el ataque es de un tipo diferente al del Pokémon que lo lanza toma un valor de 1.
    * E = Efectividad. Puede tomar los valores de 0, 0.25, 0.5, 1, 2 y 4.
    * V = Variación. Es una variable que comprende todos los valores discretos entre 85 y 100 (ambos incluidos).
     */
    public int calcularDaño(int nivelAtacante, int ataqueStat, int defensaStat, 
                            int potenciaMovimiento, double multiplicadorTipo, 
                            boolean esMismoTipo, Estado estadoAtacante, boolean esFisico) {
        // Validación de división por cero
        if (defensaStat <= 0) {
            return 1;
        }
        
        // Constante (0.2 * N + 1) = nivelFactor
        double nivelFactor = 0.2 * nivelAtacante + 1.0;

        // -- Quemadura II GEN --
        // Si está quemado y usa un ataque físico, el ataque efectivo se reduce a la mitad.
        double ataqueEfectivo = ataqueStat;
        if (estadoAtacante == Estado.QUEMADO && esFisico) {
            ataqueEfectivo = ataqueStat / 2.0;
        }
        
        // Base del daño
        // Numerador = nivelFactor * A * P
        double damageNumerator = nivelFactor * ataqueEfectivo * potenciaMovimiento;
        //Denominador = (D + 25)
        double damageDenominador = defensaStat * 25.0;

        // Daño Base
        double damageBase = (damageNumerator / damageDenominador) + 2.0;

        // Multipicadores B y E
        double bonificacionB = esMismoTipo ? 1.5 : 1.0; // STAB
        double efectividadE = multiplicadorTipo;       // Tipo
        
        // V: variación aleatoria entre 85 y 100
        double variacionV = ThreadLocalRandom.current().nextDouble(0.85, 1.0);

        // Factor final (Multiplicadores combinados + Constante 0.01))
        double modifierFinal =  bonificacionB * efectividadE * variacionV;
        
        // Daño final
        double damageFinal = damageBase * modifierFinal;

        // Retorno (Daño mímimo asegurado en 1)
        return Math.max(1, (int) Math.floor(damageFinal));
    }

    // ------------------------------------------------------------------
    // 2. LÓGICA DE ESTADO Y PRECISIÓN
    // ------------------------------------------------------------------

    /**
     * Determina si un movimiento (de daño o estado) impacta al objetivo.
     * @param precisionBase Precisión base del movimiento (0-100).
     * @return true si el ataque impacta.
     */
    public boolean verificaImpacto(int precisionBase) {
        if (precisionBase >= 100) {
            return true;
        }
        // Genera un número aleatorio entre 1 y 100.
        int hitChance = ThreadLocalRandom.current().nextInt(1, 101);
        
        return hitChance <= precisionBase;
    }

    /**
     * Determina si el ataque es un Golpe Crítico (Probabilidad base 6.25%).
     * @return true si el golpe es crítico (multiplicador x2.0).
     */
    public boolean fueGolpeCritico() {
         double probabilidadCritico = 0.0625; 
         return ThreadLocalRandom.current().nextDouble() < probabilidadCritico;
    }
    
    // ------------------------------------------------------------------
    // 3. CÁLCULO DE EXPERIENCIA (XP)
    // ------------------------------------------------------------------

    /**
     * Calcula la cantidad de experiencia ganada.
     */
    public int calcularExperiencia(int nivelDerrotado, int xpBase) {
        // Fórmula de XP simplificada: XP base * Nivel / 7
        return (int) Math.floor(xpBase * nivelDerrotado / 7.0); 
    }
}