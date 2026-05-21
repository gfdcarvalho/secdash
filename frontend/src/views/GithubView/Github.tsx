import { useState } from 'react'
import { useTranslation } from '../../i18n/I18nProvider'
import type { ExternalRepository } from '../../model/repository/externalRepository'
import Style from './Github.module.css'


export function Github() {
    const { t } = useTranslation()
    const [repositories, setRepositories] = useState<Array<ExternalRepository>>()
    const [error, setError] = useState(false)

    

    return (
        <h2> Github View</h2>
    )
}