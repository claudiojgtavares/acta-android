package cv.claudiotavares.acta.data.demo

import cv.claudiotavares.acta.data.model.*
import java.util.UUID

object DemoDataProvider {
  const val DEMO_REUNIAO_ID = "reuniao-demo-2026-ef001"
  const val DEMO_SESSAO_ID = "sessao-demo-001"
  const val DEMO_ACTA_ID = "acta-demo-v1"

  fun createDemoData(): DemoPackage {
    val agora = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 2) // Há 2 dias
    val termino = agora + (10 * 60 * 1000L + 15 * 1000L) // 10 min e 15 seg

    val reuniao = ReuniaoEntity(
      id = DEMO_REUNIAO_ID,
      titulo = "Reunião de demonstração ACTA",
      dataHoraInicio = agora,
      dataHoraTermo = termino,
      local = "Sala de demonstração (híbrida)",
      linguaOrigem = "Português",
      linguaAlvo = "Português",
      estado = "concluida"
    )

    val part1 = ParticipanteEntity(
      id = "part-mariana",
      reuniaoId = DEMO_REUNIAO_ID,
      nome = "Mariana Costa",
      funcao = "Coordenação",
      email = "mariana@example.test",
      consentimentoGravacao = true,
      consentimentoTranscricao = true
    )

    val part2 = ParticipanteEntity(
      id = "part-tiago",
      reuniaoId = DEMO_REUNIAO_ID,
      nome = "Tiago Ramos",
      funcao = "Arquitetura",
      email = "tiago@example.test",
      consentimentoGravacao = true,
      consentimentoTranscricao = true
    )

    val part3 = ParticipanteEntity(
      id = "part-sofia",
      reuniaoId = DEMO_REUNIAO_ID,
      nome = "Sofia Carvalho",
      funcao = "Produto",
      email = "sofia@example.test",
      consentimentoGravacao = true,
      consentimentoTranscricao = true
    )

    val part4 = ParticipanteEntity(
      id = "part-duarte",
      reuniaoId = DEMO_REUNIAO_ID,
      nome = "Duarte Pereira",
      funcao = "Finanças",
      email = "duarte@example.test",
      consentimentoGravacao = true,
      consentimentoTranscricao = true
    )

    val participantes = listOf(part1, part2, part3, part4)

    val sessao = SessaoEntity(
      id = DEMO_SESSAO_ID,
      reuniaoId = DEMO_REUNIAO_ID,
      instanteInicio = agora,
      instanteTermo = termino
    )

    val segmentos = listOf(
      SegmentoEntity(
        id = "seg-1",
        sessaoId = DEMO_SESSAO_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        inicioMs = 0,
        fimMs = 38000,
        texto = "Muito bom dia a todos. Damos início à reunião extraordinária do Comité de Coordenação. Informo que todos os presentes prestaram o seu consentimento prévio para a gravação e transcrição dos trabalhos nos termos do regulamento.",
        confianca = 0.98f,
        rotuloOrador = "spk_1",
        participanteId = part1.id,
        revisado = true
      ),
      SegmentoEntity(
        id = "seg-2",
        sessaoId = DEMO_SESSAO_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        inicioMs = 39000,
        fimMs = 85000,
        texto = "Obrigada, Mariana. No ponto um, relativamente à migração dos servidores centrais, concluímos os testes de carga na semana passada com êxito. Contudo, precisamos de validar a janela de transição crítica com a equipa de suporte operacional.",
        confianca = 0.96f,
        rotuloOrador = "spk_3",
        participanteId = part3.id,
        revisado = true
      ),
      SegmentoEntity(
        id = "seg-3",
        sessaoId = DEMO_SESSAO_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        inicioMs = 86000,
        fimMs = 152000,
        texto = "Confirmamos que a janela ideal será no fim de semana prolongado de novembro. Proponho formalmente fixarmos o dia 30 de novembro como data-limite improrrogável para o encerramento do datacenter legado.",
        confianca = 0.94f,
        rotuloOrador = "spk_2",
        participanteId = part2.id,
        revisado = true
      ),
      SegmentoEntity(
        id = "seg-4",
        sessaoId = DEMO_SESSAO_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        inicioMs = 153000,
        fimMs = 210000,
        texto = "Coloco à votação: todos de acordo em fixar o calendário da migração para 30 de novembro? Não havendo objeções, fica aprovado por unanimidade o novo calendário de migração para a infraestrutura de nuvem.",
        confianca = 0.97f,
        rotuloOrador = "spk_1",
        participanteId = part1.id,
        revisado = true
      ),
      SegmentoEntity(
        id = "seg-5",
        sessaoId = DEMO_SESSAO_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        inicioMs = 211000,
        fimMs = 275000,
        texto = "Do ponto de vista orçamental, o desvio foi contido. No entanto, recomendo vivamente alocarmos uma dotação extraordinária de 15.000 euros especificamente para a auditoria de segurança externa e testes de penetração.",
        confianca = 0.92f,
        rotuloOrador = "spk_4",
        participanteId = part4.id,
        revisado = true
      ),
      SegmentoEntity(
        id = "seg-6",
        sessaoId = DEMO_SESSAO_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        inicioMs = 276000,
        fimMs = 330000,
        texto = "Concordo plenamente. Consideramos formalmente deliberada a afetação dessa dotação extraordinária de 15.000 euros para a auditoria de cibersegurança e conformidade.",
        confianca = 0.95f,
        rotuloOrador = "spk_1",
        participanteId = part1.id,
        revisado = true
      ),
      SegmentoEntity(
        id = "seg-7",
        sessaoId = DEMO_SESSAO_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        inicioMs = 331000,
        fimMs = 405000,
        texto = "Eu assumo a responsabilidade de elaborar o plano detalhado de mitigação de riscos e impacto de indisponibilidade, partilhando-o com a equipa técnica até 25 de outubro.",
        confianca = 0.96f,
        rotuloOrador = "spk_2",
        participanteId = part2.id,
        revisado = true
      ),
      SegmentoEntity(
        id = "seg-8",
        sessaoId = DEMO_SESSAO_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        inicioMs = 406000,
        fimMs = 480000,
        texto = "Pela minha parte, vou recolher e comparar as propostas comerciais das três entidades certificadas de auditoria até ao dia 18 de outubro, para podermos adjudicar o serviço.",
        confianca = 0.95f,
        rotuloOrador = "spk_3",
        participanteId = part3.id,
        revisado = true
      ),
      SegmentoEntity(
        id = "seg-9",
        sessaoId = DEMO_SESSAO_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        inicioMs = 481000,
        fimMs = 540000,
        texto = "Ficou também pendente rever a conformidade das cláusulas de consentimento dos contratos com clientes até 12 de outubro. Precisamos de alguém para articular esta revisão urgente com o gabinete jurídico.",
        confianca = 0.68f, // Confiança baixa propositada para testar o filtro "por rever"
        rotuloOrador = "spk_4",
        participanteId = part4.id,
        revisado = false
      ),
      SegmentoEntity(
        id = "seg-10",
        sessaoId = DEMO_SESSAO_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        inicioMs = 541000,
        fimMs = 615000,
        texto = "Excelente. Agendamos a próxima reunião de acompanhamento para o dia 5 de novembro, pelas 10h00, na mesma sala. Declaro encerrada a sessão.",
        confianca = 0.98f,
        rotuloOrador = "spk_1",
        participanteId = part1.id,
        revisado = true
      )
    )

    val acta = ActaEntity(
      id = DEMO_ACTA_ID,
      reuniaoId = DEMO_REUNIAO_ID,
      versao = 1,
      estado = "em_revisao",
      sumulaExecutiva = "A reunião do Comité de Coordenação validou os progressos na migração para a nova infraestrutura em nuvem. Foi estabelecida a data final de transição para 30 de novembro e aprovado um reforço financeiro de 15.000€ destinado a auditorias externas de cibersegurança.",
      ordemDeTrabalhos = "1. Estado da migração dos servidores centrais.\n2. Avaliação de risco e segurança de dados.\n3. Dotação orçamental extraordinária e prazos de execução.",
      proximaReuniao = "5 de novembro de 2026, às 10h00, Sala do Conselho (Híbrida).",
      dataCriacao = agora + 650000,
      revisaoAbertaPeloMenosUmaVez = true
    )

    val deliberacoes = listOf(
      DeliberacaoEntity(
        id = "delib-1",
        actaId = DEMO_ACTA_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        versaoActa = 1,
        texto = "Aprovação unânime do calendário final de migração da infraestrutura para a nuvem com termo improrrogável em 30 de novembro.",
        ancoraInicioMs = 153000,
        ancoraFimMs = 210000,
        verificada = true
      ),
      DeliberacaoEntity(
        id = "delib-2",
        actaId = DEMO_ACTA_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        versaoActa = 1,
        texto = "Afetação de uma dotação extraordinária de 15.000€ para auditoria externa de segurança e testes de penetração.",
        ancoraInicioMs = 276000,
        ancoraFimMs = 330000,
        verificada = true
      )
    )

    val accoes = listOf(
      AccaoEntity(
        id = "accao-1",
        actaId = DEMO_ACTA_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        versaoActa = 1,
        descricao = "Elaborar o plano detalhado de mitigação de riscos de downtime e partilhar com a equipa técnica.",
        responsavelId = part2.id,
        responsavelNome = part2.nome,
        prazo = "25 de outubro de 2026",
        estado = "atribuida",
        ancoraInicioMs = 331000,
        ancoraFimMs = 405000,
        verificada = true
      ),
      AccaoEntity(
        id = "accao-2",
        actaId = DEMO_ACTA_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        versaoActa = 1,
        descricao = "Recolher e comparar propostas comerciais de três fornecedores certificados de auditoria de cibersegurança.",
        responsavelId = part3.id,
        responsavelNome = part3.nome,
        prazo = "18 de outubro de 2026",
        estado = "atribuida",
        ancoraInicioMs = 406000,
        ancoraFimMs = 480000,
        verificada = true
      ),
      AccaoEntity(
        id = "accao-3",
        actaId = DEMO_ACTA_ID,
        reuniaoId = DEMO_REUNIAO_ID,
        versaoActa = 1,
        descricao = "Rever a conformidade das cláusulas contratuais de consentimento com o gabinete jurídico.",
        responsavelId = null,
        responsavelNome = null,
        prazo = "12 de outubro de 2026",
        estado = "por atribuir", // Destacado visualmente!
        ancoraInicioMs = 481000,
        ancoraFimMs = 540000,
        verificada = true
      )
    )

    val auditorias = listOf(
      RegistoAuditoriaEntity(
        id = "aud-1",
        reuniaoId = DEMO_REUNIAO_ID,
        timestamp = agora - 10000,
        autor = "Mariana Costa",
        accao = "criacao_reuniao",
        detalhe = "Criação da reunião 'Reunião de demonstração ACTA' com 4 participantes convocados."
      ),
      RegistoAuditoriaEntity(
        id = "aud-2",
        reuniaoId = DEMO_REUNIAO_ID,
        timestamp = agora,
        autor = "Sistema ACTA",
        accao = "inicio_sessao",
        detalhe = "Verificação estrita de consentimento concluída com sucesso (4/4 participantes aptos). Início da sessão com microfone ativo."
      ),
      RegistoAuditoriaEntity(
        id = "aud-3",
        reuniaoId = DEMO_REUNIAO_ID,
        timestamp = termino,
        autor = "Sistema ACTA",
        accao = "fim_sessao",
        detalhe = "Sessão concluída com 10 segmentos capturados e transcritos em tempo real."
      ),
      RegistoAuditoriaEntity(
        id = "aud-4",
        reuniaoId = DEMO_REUNIAO_ID,
        timestamp = agora + 650000,
        autor = "Motor de IA ACTA (Gemini)",
        accao = "geracao_acta",
        detalhe = "Geração da Acta versão 1 estruturada com 2 deliberações e 3 ações com âncoras temporais validadas."
      )
    )

    return DemoPackage(
      reuniao = reuniao,
      participantes = participantes,
      sessao = sessao,
      segmentos = segmentos,
      acta = acta,
      deliberacoes = deliberacoes,
      accoes = accoes,
      auditorias = auditorias
    )
  }
}

data class DemoPackage(
  val reuniao: ReuniaoEntity,
  val participantes: List<ParticipanteEntity>,
  val sessao: SessaoEntity,
  val segmentos: List<SegmentoEntity>,
  val acta: ActaEntity,
  val deliberacoes: List<DeliberacaoEntity>,
  val accoes: List<AccaoEntity>,
  val auditorias: List<RegistoAuditoriaEntity>
)
