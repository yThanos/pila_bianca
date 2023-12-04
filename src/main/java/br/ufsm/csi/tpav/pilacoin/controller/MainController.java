package br.ufsm.csi.tpav.pilacoin.controller;

import br.ufsm.csi.tpav.pilacoin.model.MineState;
import br.ufsm.csi.tpav.pilacoin.model.Pilacoin;
import br.ufsm.csi.tpav.pilacoin.model.QueryEnvia;
import br.ufsm.csi.tpav.pilacoin.model.Usuario;
import br.ufsm.csi.tpav.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tpav.pilacoin.repository.UsuarioRepository;
import br.ufsm.csi.tpav.pilacoin.service.MineraService;
import br.ufsm.csi.tpav.pilacoin.service.ValidaService;
import br.ufsm.csi.tpav.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class MainController {
    private final RabbitTemplate rabbitTemplate;
    private final UsuarioRepository usuarioRepository;
    private final PilacoinRepository pilacoinRepository;

    public MainController(RabbitTemplate rabbitTemplate, UsuarioRepository usuarioRepository, PilacoinRepository pilacoinRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.usuarioRepository = usuarioRepository;
        this.pilacoinRepository = pilacoinRepository;
    }

    @GetMapping("/query/{type}")
    public void query(@PathVariable String type) throws JsonProcessingException {
        QueryEnvia query = QueryEnvia.builder().idQuery(1).tipoQuery(type).nomeUsuario(PilaUtil.USERNAME).build();
        ObjectMapper objectMapper = new ObjectMapper();
        rabbitTemplate.convertAndSend("query", objectMapper.writeValueAsString(query));
    }

    @GetMapping("/users")
    public List<Usuario> getUsers(){
        return usuarioRepository.findAll();
    }

    @GetMapping("/pilas")
    public List<Pilacoin> getPilas(){
        return pilacoinRepository.findAll();
    }

    @GetMapping("/mineState")
    public MineState getStates(){
        return MineState.builder().mineraBloco(MineraService.isMiningBloco())
                .mineraPila(MineraService.isMining()).validaPila(ValidaService.isValidating())
                .validaBloco(ValidaService.isValidatingBloco()).build();
    }

    @GetMapping("/mineraPila")
    public boolean mineraPila(){
        return MineraService.changeMineState();
    }

    @GetMapping("/mineraBloco")
    public boolean mineraBloco(){
        return MineraService.changeMiningBlocoState();
    }

    @GetMapping("/validaPila")
    public boolean validaPila(){
        return ValidaService.changeValidatingState();
    }

    @GetMapping("/validaBloco")
    public boolean validaBloco(){
        return ValidaService.changeValidatingState();
    }

    @PostMapping("/tranferir/{qntd}")
    public void tranferirPila(@RequestBody Usuario user){
        //ToDo: transferir
    }
}
