package br.ufsm.csi.tpav.pilacoin.repository;

import br.ufsm.csi.tpav.pilacoin.model.Pilacoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PilacoinRepository extends JpaRepository<Pilacoin, String> {
    List<Pilacoin> findByStatus(String status);
}
