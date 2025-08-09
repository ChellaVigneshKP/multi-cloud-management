"use client"

import { useState } from "react"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { cn } from "@/lib/utils"
import { LegalTabContent } from "@/components/legal/legal-tab-content"
import { tabItems } from "@/components/legal/tab-items"

export function LegalTabs() {
    const [tab, setTab] = useState("terms")

    return (
        <Tabs
            value={tab}
            onValueChange={setTab}
            className="flex flex-col lg:flex-row gap-6"
            orientation="vertical"
        >
            <div className="lg:w-64">
                <TabsList className="w-full flex lg:flex-col h-auto bg-muted/50 p-2 rounded-xl space-y-1 lg:space-y-2">
                    {tabItems.map(({ id, title }) => (
                        <TabsTrigger
                            key={id}
                            value={id}
                            className={cn(
                                "w-full justify-start px-4 py-3 text-sm font-medium rounded-md transition-colors",
                                "data-[state=active]:bg-background data-[state=active]:text-primary data-[state=active]:shadow-sm"
                            )}
                        >
                            {title}
                        </TabsTrigger>
                    ))}
                </TabsList>
            </div>

            <div className="flex-1">
                {tabItems.map(({ id, title, content }) => (
                    <LegalTabContent key={id} id={id} title={title} content={content} />
                ))}
            </div>
        </Tabs>
    )
}
