import type { Components } from 'react-markdown'

export function slugify(text: string) {
    return text.toLowerCase().replace(/[^\w\s-]/g, '').trim().replace(/\s+/g, '-')
}

export function extractMarkdownHeadings(markdown: string) {
    const lines = markdown.split('\n')
    const headings: { text: string; id: string }[] = []
    for (const line of lines) {
        const match = line.match(/^#{1,2}\s+(.+)/)
        if (match) {
            const text = match[1].replace(/[*_`]/g, '').trim()
            headings.push({ text, id: slugify(text) })
        }
    }
    return headings
}

export const markdownHeadingComponents: Components = (() => {
    const make = (Tag: 'h1' | 'h2') =>
        ({ children, ...props }: React.HTMLAttributes<HTMLHeadingElement>) => {
            const text = typeof children === 'string' ? children : ''
            return <Tag id={slugify(text)} {...props}>{children}</Tag>
        }
    return { h1: make('h1'), h2: make('h2') }
})()
