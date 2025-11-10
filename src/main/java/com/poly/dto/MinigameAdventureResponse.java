package com.poly.dto;

import lombok.Data;

@Data
public class MinigameAdventureResponse {
    private String message;           // Feedback message (e.g., "Found Stone!", "Encountered Goblin!")
    private String encounterIcon;     // Icon for the encounter (e.g., 👹, 🪨, 🪙, ✨)
    private String encounterRedirect; // URL to redirect to (e.g., /battle/1, /gathering/stone), null if no redirect
    private MinigameDataDTO updatedStats; // Player's stats AFTER the adventure action (energy deducted, gold/exp potentially gained)
}