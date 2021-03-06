package com.ss.atmlocator.controller;

import com.ss.atmlocator.entity.AtmOffice;
import com.ss.atmlocator.entity.Bank;
import com.ss.atmlocator.entity.GeoPosition;
import com.ss.atmlocator.service.ATMService;
import com.ss.atmlocator.service.BanksService;
import com.ss.atmlocator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

/**
 * Created by Vasyl Danylyuk on 29.11.2014.
 */

@Controller
@RequestMapping("/map")
public class MapController {
    @Autowired
    private ATMService atmService;
    @Autowired
    private BanksService banksService;
    @Autowired
    private UserService userService;


    @RequestMapping(value = "/getATMs")
    @ResponseBody
    public Collection<AtmOffice> getATMs(@RequestParam(required = false) Integer networkId,
                                         @RequestParam(required = false) Integer bankId,
                                         @RequestParam double userLat,
                                         @RequestParam double userLng,
                                         @RequestParam int radius,
                                         @RequestParam boolean showAtms,
                                         @RequestParam boolean showOffices
                                         ) {
        GeoPosition userPosition = new GeoPosition(userLng, userLat);
        Collection<AtmOffice> atmOffices = atmService.getATMs(networkId, bankId, showAtms, showOffices, userPosition, radius);
        return atmOffices;
    }


    @RequestMapping(value = "/getBanksByNetwork")
    @ResponseBody
    public List<Bank> getBanksByNetwork(@RequestParam Integer network_id) {
        return banksService.getBanksByNetworkId(network_id);
    }
}
