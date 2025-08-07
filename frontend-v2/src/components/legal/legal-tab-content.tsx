"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { TabsContent } from "@/components/ui/tabs"

export const LAST_UPDATED = "August 7, 2025"

export function LegalTabContent({
                                    id,
                                    title,
                                    content,
                                }: {
    id: string
    title: string
    content: React.ReactNode
}) {
    return (
        <TabsContent key={id} value={id} className="focus-visible:outline-none">
            <Card className="bg-background border border-border shadow-sm rounded-xl">
                <CardHeader className="pb-4">
                    <CardTitle className="text-3xl font-bold tracking-tight bg-gradient-to-r from-primary to-purple-600 bg-clip-text text-transparent">
                        {title}
                    </CardTitle>
                    <Separator className="my-4" />
                    <p className="text-sm text-muted-foreground">
                        Last updated: {LAST_UPDATED}
                    </p>
                </CardHeader>
                <CardContent>{content}</CardContent>
            </Card>
        </TabsContent>
    )
}