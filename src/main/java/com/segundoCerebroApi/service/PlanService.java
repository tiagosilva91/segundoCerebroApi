package com.segundoCerebroApi.service;

import com.segundoCerebroApi.config.AppProperties;
import com.segundoCerebroApi.domain.PlanType;
import com.segundoCerebroApi.domain.User;
import com.segundoCerebroApi.dto.PlanUsageDTO;
import com.segundoCerebroApi.exception.PlanLimitExceededException;
import com.segundoCerebroApi.repository.NoteRepository;
import com.segundoCerebroApi.repository.ThemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Centraliza as regras de uso por plano.
 * FREE: limites configuráveis (app.plan.free.*). PRO: ilimitado.
 */
@Service
public class PlanService {

    private final NoteRepository noteRepository;
    private final ThemeRepository themeRepository;
    private final AppProperties.Plan.Limits freeLimits;

    public PlanService(NoteRepository noteRepository, ThemeRepository themeRepository, AppProperties props) {
        this.noteRepository = noteRepository;
        this.themeRepository = themeRepository;
        this.freeLimits = props.plan().free();
    }

    public boolean isPro(User user) {
        return user.getPlanType() == PlanType.PRO;
    }

    /** Limite de notas do plano do usuário, ou null se ilimitado. */
    public Integer noteLimit(User user) {
        return isPro(user) ? null : freeLimits.maxNotes();
    }

    /** Limite de temas do plano do usuário, ou null se ilimitado. */
    public Integer themeLimit(User user) {
        return isPro(user) ? null : freeLimits.maxThemes();
    }

    /** Lança exceção se o usuário não puder criar mais notas. Retorna a contagem atual. */
    public long assertCanCreateNote(User user) {
        long count = noteRepository.countByUser(user);
        Integer limit = noteLimit(user);
        if (limit != null && count >= limit) {
            throw new PlanLimitExceededException(
                    "Você atingiu o limite de " + limit + " notas do plano FREE. Faça upgrade para o PRO para criar notas ilimitadas.");
        }
        return count;
    }

    /** Lança exceção se o usuário não puder criar mais temas. Retorna a contagem atual. */
    public long assertCanCreateTheme(User user) {
        long count = themeRepository.countByUser(user);
        Integer limit = themeLimit(user);
        if (limit != null && count >= limit) {
            throw new PlanLimitExceededException(
                    "Você atingiu o limite de " + limit + " temas do plano FREE. Faça upgrade para o PRO para criar temas ilimitados.");
        }
        return count;
    }

    /** Mensagem de aviso quando o recurso recém-criado foi o último permitido pelo plano. */
    public String lastItemWarning(User user, long countBeforeCreate, Integer limit, String resource) {
        if (limit != null && countBeforeCreate + 1 == limit) {
            return "Este foi o seu último " + resource + " do plano FREE. Deseja migrar para o plano PRO?";
        }
        return null;
    }

    @Transactional(readOnly = true)
    public PlanUsageDTO usage(User user) {
        long notes = noteRepository.countByUser(user);
        long themes = themeRepository.countByUser(user);
        Integer nl = noteLimit(user);
        Integer tl = themeLimit(user);
        return new PlanUsageDTO(
                user.getPlanType(),
                new PlanUsageDTO.Usage(notes, nl, nl == null || notes < nl),
                new PlanUsageDTO.Usage(themes, tl, tl == null || themes < tl)
        );
    }
}
