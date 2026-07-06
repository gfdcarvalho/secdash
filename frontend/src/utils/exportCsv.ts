export type CsvValue = string | number | null | undefined

export interface CsvReport {
    title: string
    metadata: [string, CsvValue][]      // pares label/valor com informação do repositório/equipa e data de geração
    summaryTitle: string
    summary: [string, CsvValue][]       // pares label/valor com os totais (por severidade, estado, etc.)
    headers: string[]                   // cabeçalhos da tabela de dados
    rows: CsvValue[][]                  // linhas da tabela de dados
}

const SEPARATOR = ','

// escapa um valor segundo o RFC 4180: envolve em aspas se contiver separador, aspas ou quebras de linha
function escapeCsvValue(value: CsvValue): string {
    if (value === null || value === undefined) return ''
    const text = String(value)
    if (text.includes(SEPARATOR) || text.includes('"') || text.includes('\n') || text.includes('\r')) {
        return `"${text.replace(/"/g, '""')}"`
    }
    return text
}

function toCsvLine(values: CsvValue[]): string {
    return values.map(escapeCsvValue).join(SEPARATOR)
}

// constrói o conteúdo do relatório: título, metadados, resumo e tabela de dados separados por linhas em branco
export function buildCsvReport(report: CsvReport): string {
    const lines: string[] = []

    lines.push(toCsvLine([report.title]))
    report.metadata.forEach(([label, value]) => lines.push(toCsvLine([label, value])))
    lines.push('')

    lines.push(toCsvLine([report.summaryTitle]))
    report.summary.forEach(([label, value]) => lines.push(toCsvLine([label, value])))
    lines.push('')

    lines.push(toCsvLine(report.headers))
    report.rows.forEach(row => lines.push(toCsvLine(row)))

    return lines.join('\r\n')
}

// descarrega o conteúdo como ficheiro CSV; o BOM UTF-8 garante que o Excel interpreta corretamente os acentos
export function downloadCsv(filename: string, content: string) {
    const blob = new Blob(['\uFEFF' + content], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
}

// normaliza um nome para ser usado no nome do ficheiro (remove acentos e caracteres especiais)
export function sanitizeFilename(name: string): string {
    return name
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-zA-Z0-9-_]+/g, '-')
        .replace(/^-+|-+$/g, '')
        .toLowerCase()
}

// data atual no formato YYYY-MM-DD para o nome do ficheiro
export function currentDateStamp(): string {
    return new Date().toISOString().slice(0, 10)
}
