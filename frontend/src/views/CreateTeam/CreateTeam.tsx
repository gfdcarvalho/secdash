import { useState } from 'react'
import { useNavigate } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import style from './CreateTeam.module.css'
import { api } from '../../utils/fetchApi'
import type { Team } from '../../model/teams/teams'
import { isSuccess } from '../../utils/Either'

export function CreateTeam() {
    const navigate = useNavigate()
    const { t } = useTranslation()
    const [name, setName] = useState("")
    const [description, setDescription] = useState("")
    const [nameError, setNameError] = useState<keyof typeof t.createTeam.errors | "">("")
    const [isLoading, setIsLoading] = useState(false)

    const handleSubmit = async () => {
        if (!name) {
            setNameError("blankName")
            return
        }
        setIsLoading(true)
        const response = await api.post<Team>("/teams", {name, description})
        if (isSuccess(response)) {
            navigate("/teams")
        } else {
            setIsLoading(false)
        }
    }

    return (
        <div className={style.createTeamContent}>
            <div className={style.topBar}>
                <h2>{t.createTeam.title}</h2>
                <button>{t.createTeam.myTeams}</button>
            </div>
            <div className={style.createForm}>
                <div className={style.fieldWrapper}>
                    <input
                        className="inputText"
                        type="text"
                        placeholder={t.createTeam.namePlaceholder}
                        value={name}
                        onChange={e => { setName(e.target.value); setNameError("") }}
                    />
                    {nameError && <p className={style.error}>{t.createTeam.errors[nameError]}</p>}
                </div>
                <input
                    className="inputText"
                    type="text"
                    placeholder={t.createTeam.descriptionPlaceholder}
                    value={description}
                    onChange={e => setDescription(e.target.value)}
                />
                <div className={style.createFormButtons}>
                    <button className={style.createTeamButton} onClick={handleSubmit} disabled={isLoading}>
                        {t.createTeam.submit}
                    </button>
                </div>
            </div>
        </div>
    )
}
