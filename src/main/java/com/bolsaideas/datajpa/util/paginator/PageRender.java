package com.bolsaideas.datajpa.util.paginator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

public class PageRender<T> {

	private String url;
	private Page<T> page;
	private int totalPage;
	private int numberElementForPage;
	private int actualPage;
	private List<PageItem> pages;

	public PageRender(String url, Page<T> page) {
		this.url = url;
		this.page = page;
		this.pages = new ArrayList<PageItem>();

		numberElementForPage = page.getSize();
		totalPage = page.getTotalPages();
		actualPage = page.getNumber() + 1;

		int desde, hasta;

		if (totalPage <= numberElementForPage) {
			desde = 1;
			hasta = totalPage;
		} else {
			if (actualPage <= numberElementForPage / 2) {
				desde = 1;
				hasta = numberElementForPage;
			} else if (actualPage >= totalPage - numberElementForPage / 2) {
				desde = totalPage - numberElementForPage + 1;
				hasta = totalPage;
			} else {
				desde = actualPage - numberElementForPage / 2;
				hasta = actualPage + numberElementForPage / 2;
			}
		}

		for (int i = desde; i <= hasta; i++) {
			pages.add(new PageItem(i, i == actualPage));
		}
	}

	public String getUrl() {
		return url;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public int getActualPage() {
		return actualPage;
	}

	public List<PageItem> getPages() {
		return pages;
	}

	public boolean isFirst() {
		return page.isFirst();
	}

	public boolean isLast() {
		return page.isLast();
	}

	public boolean isHasNext() {
		return page.hasNext();
	}

	public boolean isHasPrevious() {
		return page.hasPrevious();
	}
}

