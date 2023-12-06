package br.ufsm.csi.tpav.pilacoin.controller;

import br.ufsm.csi.tpav.pilacoin.model.*;
import br.ufsm.csi.tpav.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tpav.pilacoin.repository.UsuarioRepository;
import br.ufsm.csi.tpav.pilacoin.service.MineraService;
import br.ufsm.csi.tpav.pilacoin.service.RabbitService;
import br.ufsm.csi.tpav.pilacoin.service.ValidaService;
import br.ufsm.csi.tpav.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/teste")
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
        MineState state = MineState.builder().mineraBloco(MineraService.isMiningBloco())
                .mineraPila(MineraService.isMining()).validaPila(ValidaService.isValidating())
                .validaBloco(ValidaService.isValidatingBloco()).build();
        System.out.println(state);
        return state;
    }

    @GetMapping("/mineraPila")
    public boolean mineraPila(){
        System.out.println("Alterando mineracao de pila");
        return MineraService.changeMineState();
    }

    @GetMapping("/mineraBloco")
    public boolean mineraBloco(){
        System.out.println("Alterando mineracao de bloco");
        return MineraService.changeMiningBlocoState();
    }

    @GetMapping("/validaPila")
    public boolean validaPila(){
        System.out.println("Alterando validacao de pila");
        return ValidaService.changeValidatingState();
    }

    @GetMapping("/validaBloco")
    public boolean validaBloco(){
        System.out.println("Alterando validacao de bloco");
        return ValidaService.changeValidatingBlocoState();
    }

    @PostMapping("/tranferir/{qntd}")
    public void tranferirPila(@RequestBody Usuario user, @PathVariable int qntd) throws JsonProcessingException {
        //ToDo: transferir
        List<Pilacoin> pilas = pilacoinRepository.findByStatus("VALIDO");
        if (pilas.size() < qntd){
            throw new RuntimeException();
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < qntd; i++){
                TransferirPilacoin tranferir = TransferirPilacoin.builder().noncePila(pilas.get(i).getNonce())
                        .chaveUsuarioOrigem(PilaUtil.PUBLIC_KEY.getEncoded()).nomeUsuarioOrigem(PilaUtil.USERNAME)
                        .chaveUsuarioDestino(user.getChavePublica()).nomeUsuarioDestino(user.getNome())
                        .dataTransacao(new Date()).build();
                tranferir.setAssinatura(PilaUtil.geraAssinatura(tranferir));
                rabbitTemplate.convertAndSend("transferir-pila", objectMapper.writeValueAsString(tranferir));
                pilacoinRepository.delete(pilas.get(i));
            }
        }
    }

    @GetMapping("/logs")
    public List<Mensagem> getLogs(){
        return RabbitService.msgs;
    }
}
