package cl.ubiobio.silkcorp.polygen_research.controller;

import cl.ubiobio.silkcorp.polygen_research.entity.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.service.WhitelistService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/whitelist")
public class WhitelistController {

    private final WhitelistService whitelistService;

    public WhitelistController(WhitelistService whitelistService) {
        this.whitelistService = whitelistService;
    }

    @PostMapping("/register")
    public Whitelist createCredencial(@RequestBody Whitelist credencial) {
        return whitelistService.saveCredencial(credencial);
    }
    
    // El endpoint de "/login" se manejaría de forma diferente,
    // usualmente con Spring Security.
}