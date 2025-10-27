    package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf;

    import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
    import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf;
    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.FetchType;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import jakarta.persistence.JoinColumn;
    import jakarta.persistence.ManyToOne;
    import jakarta.persistence.Table;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    @Entity
    @Table(name = "datos_crf")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class DatosCrf {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_detalle")
        private Integer idDetalle;

        @Column(name = "valor", length = 30)
        private String valor;

        // --- Clave Foránea (Relación N-a-1) a Crf ---
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "crf_id", nullable = false)
        private Crf crf;

        // --- Clave Foránea (Relación N-a-1) a CampoCrf ---
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "campo_crf_id", nullable = false)
        private CampoCrf campoCrf;

        public Integer getIdDetalle() {
            return idDetalle;
        }

        public void setIdDetalle(Integer idDetalle) {
            this.idDetalle = idDetalle;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }

        public Crf getCrf() {
            return crf;
        }

        public void setCrf(Crf crf) {
            this.crf = crf;
        }

        public CampoCrf getCampoCrf() {
            return campoCrf;
        }

        public void setCampoCrf(CampoCrf campoCrf) {
            this.campoCrf = campoCrf;
        }
    }