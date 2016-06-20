package es.unizar.iaaa.geofencing.web;

import com.fasterxml.jackson.annotation.JsonView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import es.unizar.iaaa.geofencing.model.Rule;
import es.unizar.iaaa.geofencing.repository.RuleRepository;
import es.unizar.iaaa.geofencing.view.View;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

@RestController
public class RuleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleController.class);
    @Autowired
    private RuleRepository ruleRepository;

    /**
     * This method creates a new rule.
     *
     * @param rule data of the rule
     * @return the rule created
     */
    @RequestMapping(path = "/api/rules", method = RequestMethod.POST)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Rule created",
                    responseHeaders = @ResponseHeader(name = "Location", description = "Location",
                            response = URI.class), response = Rule.class)})
    public ResponseEntity<Rule> createRule(@RequestBody final Rule rule) {
        LOGGER.info("Requested /api/rules POST method");
        rule.setId(null);
        Rule ruleCreated = ruleRepository.save(rule);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(rule.getId()).toUri());
        return new ResponseEntity<>(ruleCreated, httpHeaders, HttpStatus.CREATED);
    }

    /**
     * This method modifies the data of a previously created rule.
     *
     * @param id   unique identifier representing a specific rule
     * @param rule data of the rule
     * @return the rule modified
     */
    @RequestMapping(path = "/api/rules/{id}", method = RequestMethod.PUT)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Rule modified", response = Rule.class),
            @ApiResponse(code = 409, message = "Rule state doesn't permit request", response = RuleResourceConflictException.class),
            @ApiResponse(code = 404, message = "Rule not found", response = RuleNotFoundException.class)})
    @JsonView(View.RuleCompleteView.class)
    public Rule modifyRule(@PathVariable("id") Long id, @RequestBody Rule rule) {
        LOGGER.info("Requested /api/rules/{id} PUT method");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails customUser = (UserDetails) auth.getPrincipal();
        String nick = customUser.getUsername();
        if (!ruleRepository.existsByUsername(id, nick)) {
            throw new RuleNotFoundException();
        }
        Rule ruleRequested = ruleRepository.findOne(id);
        ruleRequested.setEnabled(rule.getEnabled());
        ruleRequested.setType(rule.getType());
        ruleRequested.setTime(rule.getTime());
        ruleRequested.setMessage(rule.getMessage());
        ruleRequested.setDays(rule.getDays());
        try {
            return ruleRepository.save(ruleRequested);
        } catch (Exception e) {
            throw new RuleResourceConflictException(id, e);
        }
    }

    /**
     * This method deletes the data of a previously created rule.
     *
     * @param id unique identifier representing a specific rule
     * @return the rule deleted
     */
    @RequestMapping(path = "/api/rules/{id}", method = RequestMethod.DELETE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Rule deleted", response = Rule.class),
            @ApiResponse(code = 404, message = "Rule not found", response = RuleNotFoundException.class)})
    public Rule deleteRule(@PathVariable("id") Long id) {
        LOGGER.info("Requested /api/rules/{id} DELETE method");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails customUser = (UserDetails) auth.getPrincipal();
        String nick = customUser.getUsername();
        if (ruleRepository.existsByUsername(id, nick)) {
            ruleRepository.delete(id);
            return null;
        } else {
            throw new RuleNotFoundException();
        }
    }

    /**
     * This method returns a rule by id.
     *
     * @param id unique identifier representing a specific rule
     * @return the rule requested
     */
    @RequestMapping(path = "/api/rules/{id}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Rule requested", response = MappingJacksonValue.class),
            @ApiResponse(code = 404, message = "Rule not found", response = RuleNotFoundException.class)})
    public MappingJacksonValue getRule(@PathVariable("id") Long id) {
        LOGGER.info("Requested /api/rules/{id} GET method");
        if (ruleRepository.exists(id)) {
            final MappingJacksonValue result = new MappingJacksonValue(ruleRepository.findOne(id));
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if ((auth instanceof AnonymousAuthenticationToken)) {
                result.setSerializationView(View.RuleBaseView.class);
            } else {
                result.setSerializationView(View.RuleCompleteView.class);
            }
            return result;
        } else {
            throw new RuleNotFoundException();
        }
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Rule status forbids request")
    public class RuleResourceConflictException extends RuntimeException {
        public RuleResourceConflictException(Long id, Exception e) {
            super("Failed update of Rule " + id, e);
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such Rule")
    public class RuleNotFoundException extends RuntimeException {
    }
}